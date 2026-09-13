package open.dolphin;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.hibernate7.Hibernate7Module;
import jakarta.ejb.Singleton;
import jakarta.ws.rs.ext.ContextResolver;
import jakarta.ws.rs.ext.Provider;
import open.dolphin.util.JsonUtils;

/// Jackson ObjectMapper
///
/// @author pns
@Provider
@Singleton
public class JsonConverter implements ContextResolver<ObjectMapper> {
    private final ObjectMapper mapper;
    private final Hibernate7Module hbm;

    public JsonConverter() {
        mapper = new ObjectMapper();
        hbm = new Hibernate7Module();
        initModule();
        System.out.println("[open.dolphin.JsonConverter] ObjectMapper configured.");
    }

    private void initModule() {
        JsonUtils.initialilze(mapper);

        // works in Hibernate 7
        hbm.configure(Hibernate7Module.Feature.FORCE_LAZY_LOADING, false);
        hbm.configure(Hibernate7Module.Feature.USE_TRANSIENT_ANNOTATION, false);
        mapper.registerModule(hbm);
    }

    /// Provides ObjectMapper for resteasy.
    ///
    /// @param type the class of object for which a context is desired
    /// @return a context for the supplied type or `null`
    @Override
    public ObjectMapper getContext(Class<?> type) {
        return mapper;
    }
}
