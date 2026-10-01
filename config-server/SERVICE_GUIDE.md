# Config Server Guide

## Purpose
Config Server centralizes shared runtime configuration so services do not need every setting duplicated locally.

## Runtime
- Port: `8888`
- Profile: native file-based configuration
- Main class: `ConfigServerApplication`

## Endpoints
Spring Cloud Config exposes configuration by application and profile, for example:
- `GET /payment-service/default`
- `GET /notification-service/default`

## Stored configuration
- `config/application.properties`: shared Eureka, JWT, RabbitMQ, and OAuth2 placeholders
- `config/payment-service.properties`: Razorpay and bank encryption settings
- `config/notification-service.properties`: SMTP settings

## Client behavior
Order, Payment, and Notification Services use:
`spring.config.import=optional:configserver:${CONFIG_SERVER_URL:http://localhost:8888}`

The import is optional for local development, so a service can still start when Config Server is temporarily unavailable. In a production deployment, make the import mandatory and protect Config Server.

## OAuth2 status
OAuth2 provider placeholders are present for future Google login. JWT remains the active authentication mechanism. Adding provider credentials alone does not replace the current login flow; OAuth2 security endpoints still need to be deliberately enabled.
