package open.dolphin.infomodel;

import jakarta.persistence.SequenceGenerator;

/// InfoModel.
/// ```
/// select increment_by from pg_sequences where schemaname = 'public' and sequencename = 'hibernate_sequence';
///  increment_by
/// --------------
///             1
/// ```
///
/// @author Minagawa, Kazushi
@SequenceGenerator(
        name = InfoModel.GENERATOR_NAME,
        sequenceName = InfoModel.SEQUENCE_NAME,
        allocationSize = 1
)
public class InfoModel implements IInfoModel {
    public static final String GENERATOR_NAME = "hibernate_sequence_generator";
    public static final String SEQUENCE_NAME = "hibernate_sequence";
}
