package custom.sales_invoices;

import java.util.UUID;

import org.eclipse.dirigible.sdk.component.Component;
import org.eclipse.dirigible.sdk.db.CalculatedField;

/**
 * Server-side generator for the SalesInvoice number. Replaces the previous inline
 * {@code java.util.UUID.randomUUID().toString()} calculated expression with a hand-written call-out
 * so the real codbex NumberGeneratorService logic (prefix + sequence per series) can drop in here
 * without touching the model. It implements {@code CalculatedField<Object, String>} because it does
 * not read the invoice - swap the bound type to the generated {@code SalesInvoiceEntity} if the
 * algorithm needs the record's fields.
 *
 * Referenced from the SalesInvoice entity's Imports in sales-invoices.intent and invoked by the
 * generated repository as {@code Beans.get(SalesInvoiceNumberAction.class).calculate(entity)}.
 */
@Component
public class SalesInvoiceNumberAction implements CalculatedField<Object, String> {

    @Override
    public String calculate(Object entity) {
        return UUID.randomUUID()
                   .toString();
    }

}
