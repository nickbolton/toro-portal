package net.unicon.resource.thread;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import net.unicon.resource.Resource;

public class GuaranteedActionSupport {

    protected final Log log = LogFactory.getLog(getClass());

    public String describe(GuaranteedAction action) {
        if (action == null || action.isSuccessful()) {
            return "";
        } else if (action.timedOut()) {
            return "timeout";
        } else if (action.erroredOut()) {
            return "error: " + action.getThrowable();
        } else if (action.interrupted()) {
            return "interrupted";
        }

        return "";
    }

    public void log(GuaranteedAction action, String actionString, Resource resource) {
        if (action.timedOut()) {
            if (log.isInfoEnabled()) {
            log.info("The action [" + action + "] (" + actionString + "] timed out on resource [" + resource + "] in resource pool [" + this + "]");
            }
        } else if (action.erroredOut()) {
            log.warn("The action [" + action + "] (" + actionString + ") errored out on resource [" + resource + "] in resource pool [" + this + "]", action.getThrowable());
        } else if (action.interrupted()) {
            log.info("The action [" + action + "] (" + actionString + ") was interrupted in resource [" + resource + "] in resource pool [" + this + "]");
        }
    }

}
