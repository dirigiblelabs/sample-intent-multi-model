package custom.numbers;

import java.util.List;

import org.eclipse.dirigible.components.data.store.java.repository.Criteria;

import gen.numbers.data.settings.NumberEntity;
import gen.numbers.data.settings.NumberRepository;

/**
 * Reusable document-number generator - a client-Java port of codbex/codbex-number-generator's
 * {@code Generator} service. Each {@code Number} row (one per document {@code Type}) carries a prefix,
 * a total length, and the last issued counter ({@code Value}); {@link #generateByType(String)}
 * increments that counter and returns the formatted number, e.g. type {@code "Sales Invoice"} (prefix
 * {@code SI}, length {@code 10}) yields {@code SI00000001}.
 *
 * <p>
 * Hand-written under {@code custom/} (the intent escape hatch): it is never generated or scrubbed. It
 * is entity-agnostic (it knows only its own {@code Number} series entity), so it lives here in the
 * {@code numbers} project and is reused by any document delegate in any project that depends on it -
 * the caller (e.g. {@code sales-invoices}' {@code DocumentNumberGeneratorDelegate}) is the one that
 * touches the document entity through that document's own generated repository.
 */
public class DocumentNumberGenerator {

    private final NumberRepository numberRepository = new NumberRepository();

    /**
     * Generate the next number for the given series type (e.g. {@code "Sales Invoice"}), incrementing
     * and persisting the series counter through the repository.
     *
     * @param type the series type, matching a {@code Number.Type} row
     * @return the formatted number, or {@code null} when no series is configured for the type
     */
    public String generateByType(String type) {
        List<NumberEntity> matches = numberRepository.findAll(Criteria.create()
                                                                      .eq("Type", type));
        if (matches.isEmpty()) {
            return null;
        }
        return generateNumber(matches.get(0));
    }

    private String generateNumber(NumberEntity number) {
        long next = (number.Value == null ? 0L : number.Value) + 1;
        number.Value = next;
        numberRepository.update(number);

        String prefix = number.Prefix == null ? "" : number.Prefix;
        int total = number.Length == null ? 0 : number.Length;
        int digits = Math.max(total - prefix.length(), 1);
        return prefix + String.format("%0" + digits + "d", next);
    }
}
