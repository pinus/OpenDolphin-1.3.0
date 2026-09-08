package open.dolphin.infomodel;

import org.hibernate.search.mapper.pojo.bridge.RoutingBridge;
import org.hibernate.search.mapper.pojo.bridge.binding.RoutingBindingContext;
import org.hibernate.search.mapper.pojo.bridge.mapping.programmatic.RoutingBinder;
import org.hibernate.search.mapper.pojo.bridge.runtime.RoutingBridgeRouteContext;
import org.hibernate.search.mapper.pojo.route.DocumentRoutes;

/**
 *
 * @author masuda, Masudana Ika
 */
public class DocumentRoutingBinder implements RoutingBinder, RoutingBridge<DocumentModel> {

    @Override
    public void bind(RoutingBindingContext context) {
        context.dependencies().use("status");
        context.bridge(DocumentModel.class, this);
    }

    @Override
    public void route(DocumentRoutes routes, Object eid,
                      DocumentModel document, RoutingBridgeRouteContext context) {
        switch (document.getStatus()) {
            case IInfoModel.STATUS_FINAL:
            case IInfoModel.STATUS_TMP:
                routes.addRoute();
                break;
            default:
                routes.notIndexed();
                break;
        }
    }

    @Override
    public void previousRoutes(DocumentRoutes routes, Object eid,
                               DocumentModel document, RoutingBridgeRouteContext context) {
        routes.addRoute();
    }
}
