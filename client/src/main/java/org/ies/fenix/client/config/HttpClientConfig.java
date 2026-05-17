package org.ies.fenix.client.config;

import org.ies.fenix.controller.IClientController;
import org.ies.fenix.controller.IGameController;
import org.ies.fenix.controller.IPurchaseController;
import org.ies.fenix.controller.ITagController;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.service.registry.ImportHttpServices;

@Configuration
@ImportHttpServices(group = "backend", types = {IClientController.class, ITagController.class, IPurchaseController.class, IGameController.class})
public class HttpClientConfig {
}