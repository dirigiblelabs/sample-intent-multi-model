package custom.sales_invoices;

import org.flowable.common.engine.impl.el.FixedValue;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;

import custom.numbers.DocumentNumberGenerator;
import gen.sales_invoices.data.salesinvoice.SalesInvoiceEntity;
import gen.sales_invoices.data.salesinvoice.SalesInvoiceRepository;

/**
 * BPMN service-task delegate that stamps the sales-invoice number when the invoice is issued. Bound to
 * the {@code generateNumber} step of the SalesInvoiceApproval process via
 * {@code flowable:class="custom.sales_invoices.DocumentNumberGeneratorDelegate"}, with the number-series
 * {@code type} injected as a {@code flowable:field}.
 *
 * <p>
 * <b>Why it lives in sales-invoices (not the shared numbers project):</b> it must load and save the
 * SalesInvoice through the generated {@link SalesInvoiceRepository} - the only sanctioned path, since
 * the repository carries validations, event publishing, and multi-language support that the generic
 * Store API would silently skip. A delegate that touches an entity therefore belongs in that entity's
 * own project. The entity-agnostic part - turning a series type into the next formatted number - is the
 * reusable {@link DocumentNumberGenerator} in the numbers project.
 *
 * <p>
 * Hand-written under {@code custom/}; never generated or scrubbed.
 */
public class DocumentNumberGeneratorDelegate implements JavaDelegate {

    /** The number-series type (e.g. "Sales Invoice"), injected from the service task's flowable:field. */
    public FixedValue type;

    private final SalesInvoiceRepository repository = new SalesInvoiceRepository();

    @Override
    public void execute(DelegateExecution execution) {
        Object key = execution.getVariable("Id");
        if (!(key instanceof Number)) {
            return;
        }
        SalesInvoiceEntity invoice = repository.findById(((Number) key).intValue());
        if (invoice == null) {
            return;
        }
        String seriesType = type == null ? null : type.getExpressionText();
        String number = new DocumentNumberGenerator().generateByType(seriesType);
        if (number == null) {
            return;
        }
        invoice.Number = number;
        // Workflow-driven system write: persist through the repository (keeps validations/i18n) but
        // without re-publishing the "-updated" event, exactly like the intent SetField/Writer delegates.
        repository.updateWithoutEvent(invoice);
    }
}
