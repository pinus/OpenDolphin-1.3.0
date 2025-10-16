package open.dolphin.impl.pvt;

import open.dolphin.infomodel.PatientVisitModel;
import open.dolphin.util.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.websocket.CloseReason;
import jakarta.websocket.Endpoint;
import jakarta.websocket.EndpointConfig;
import jakarta.websocket.Session;

/**
 * @author pns
 */
public class PvtEndpoint extends Endpoint {

    private PvtListener pvtListener;
    private final Logger logger = LoggerFactory.getLogger(PvtEndpoint.class);

    public PvtEndpoint() {
        super();
    }

    public void addPvtListener(PvtListener listener) {
        pvtListener = listener;
    }

    @Override
    public void onOpen(Session session, EndpointConfig config) {
        session.addMessageHandler((PvtMessageHandler) message -> {
            PatientVisitModel hostPvt = JsonUtils.fromJson(message, PatientVisitModel.class);
            pvtListener.pvtChanged(hostPvt);
        });
    }

    @Override
    public void onError(Session session, Throwable t) {
        logger.info("websocket error");
        t.printStackTrace(System.err);
    }

    @Override
    public void onClose(Session session, CloseReason reason) {
        logger.info("websocket closed: {}", reason.getReasonPhrase());
    }
}
