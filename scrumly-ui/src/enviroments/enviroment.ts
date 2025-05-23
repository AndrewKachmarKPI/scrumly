export const environment = {
  production: false,
  keycloak: {
    authority: 'http://localhost:9999',
    redirectUri: 'http://localhost:4200',
    postLogoutRedirectUri: 'http://localhost:4200',
    realm: 'scrumly',
    clientId: 'scrumly-ui',
  },
  api_url: 'http://localhost:8072',
  WS: {
    conference_service: 'http://localhost:8072/conference/ws',
    room_service: 'http://localhost:8072/room/ws'
  }
}
