import {KeycloakService} from 'keycloak-angular';
import {environment} from "../../../enviroments/enviroment";


export const initializeKeycloak = (keycloak: KeycloakService) => async () =>
  keycloak.init({
    config: {
      url: environment.keycloak.authority,
      realm: environment.keycloak.realm,
      clientId: environment.keycloak.clientId,
    },
    loadUserProfileAtStartUp: true,
    bearerPrefix: "Bearer",
    initOptions: {
      onLoad: 'check-sso',
      enableLogging: true,
      checkLoginIframe: false,
      // redirectUri: environment.keycloak.redirectUri,
    },
  });
