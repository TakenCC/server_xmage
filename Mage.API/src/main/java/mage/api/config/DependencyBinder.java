package mage.api.config;

import mage.api.service.AuthenticationService;
import mage.api.service.JwtService;
import mage.server.MageServerImpl;
import mage.server.managers.ManagerFactory;
import org.glassfish.hk2.utilities.binding.AbstractBinder;

public class DependencyBinder extends AbstractBinder {

    private final ManagerFactory managerFactory;
    private final MageServerImpl mageServer;
    private final JwtService jwtService;
    private final AuthenticationService authenticationService;

    public DependencyBinder(ManagerFactory managerFactory) {
        this.managerFactory = managerFactory;
        this.mageServer = new MageServerImpl(managerFactory, "", false, false);
        this.jwtService = new JwtService();
        this.authenticationService = new AuthenticationService(managerFactory, jwtService);
    }

    @Override
    protected void configure() {
        bind(managerFactory).to(ManagerFactory.class);
        bind(mageServer).to(MageServerImpl.class);
        bind(jwtService).to(JwtService.class);
        bind(authenticationService).to(AuthenticationService.class);
    }
}

