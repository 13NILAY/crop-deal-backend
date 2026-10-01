# Subscription Service Guide

## Purpose

Subscription Service stores dealer subscriptions to farmer crop listings. It allows a dealer to follow a farmer and supports notification workflows by returning the dealers subscribed to a farmer.

The service owns subscription records only. It does not own dealer accounts, farmer profiles, or crop listings.

## Runtime

- Port: `8090`
- Database: `cropdeal_subscription`
- Main API prefix: `/subscriptions`
- Service discovery: Eureka
- Authentication: JWT
- Internal integration: `X-Internal-Token`

## Data Model

The `crop_subscriptions` table contains:

- `id`: generated subscription identifier
- `dealerEmail`: authenticated dealer email
- `farmerId`: farmer profile identifier
- `createdAt`: subscription creation timestamp

A unique constraint prevents the same dealer from subscribing to the same farmer more than once.

## APIs

### Subscribe to a farmer

```http
POST /subscriptions/farmers/{farmerId}
Authorization: Bearer <dealer-jwt>
```

Required role: `DEALER`.

The operation is idempotent. If the dealer is already subscribed to the farmer, the existing subscription is returned.

### Unsubscribe from a farmer

```http
DELETE /subscriptions/farmers/{farmerId}
Authorization: Bearer <dealer-jwt>
```

Required role: `DEALER`.

The subscription is removed when it exists. The endpoint returns `204 No Content`.

### List the authenticated dealer's subscriptions

```http
GET /subscriptions/mine
Authorization: Bearer <dealer-jwt>
```

Required role: `DEALER`.

Subscriptions are returned in newest-first order.

### Find dealers subscribed to a farmer

```http
GET /subscriptions/farmers/{farmerId}/dealers
X-Internal-Token: <subscription-internal-token>
```

This endpoint returns a list of subscribed dealer email addresses. It is intended for trusted service-to-service calls, such as notification processing.

The endpoint currently has a public HTTP security rule, but the controller requires a matching `X-Internal-Token`. Requests without the correct token are rejected.

## Typical Workflow

1. A dealer logs in through Authentication Service and receives a JWT containing the `DEALER` role.
2. The dealer subscribes to a farmer using `POST /subscriptions/farmers/{farmerId}`.
3. The subscription is stored in the subscription database.
4. When a farmer's crop event needs subscriber notifications, a trusted service calls `GET /subscriptions/farmers/{farmerId}/dealers`.
5. Notification Service can use the returned email addresses to create notifications or send email.
6. The dealer can view or remove subscriptions through `/subscriptions/mine` and the unsubscribe endpoint.

## Configuration

Important properties include:

```properties
server.port=8090
spring.datasource.url=jdbc:mysql://localhost:3306/cropdeal_subscription
spring.datasource.username=${DB_USERNAME:Nilay}
spring.datasource.password=${DB_PASSWORD:Nilay}
security.jwt.secret=${JWT_SECRET:...}
subscription.internal-token=${SUBSCRIPTION_INTERNAL_TOKEN:...}
```

Use environment variables for database credentials, the JWT secret, and the internal token outside local development. The JWT secret must match the secret used by the other services that issue or validate the token.

## Security

- Public health and API documentation endpoints are permitted.
- Subscribe, unsubscribe, and personal subscription listing require the `DEALER` role.
- Farmer profile IDs are stored as references; farmer ownership and profile details remain in Profile Service.
- Internal dealer lookup requires `X-Internal-Token`.

## Current Limitations

- Subscriptions are currently farmer-wide; dealers cannot filter by crop type, location, price, quantity, or harvest date.
- There is no subscription expiration or pause feature.
- The service does not publish subscription events by itself.
- Notification delivery is handled by Notification Service and its event listeners.
- The internal token is shared configuration rather than a rotating service identity.

## Potential Extensions

- Add crop type, location, price, quantity, and harvest-window filters.
- Publish subscription-created and subscription-removed events.
- Add targeted crop alerts when a matching listing is created.
- Add subscription pause, expiry, and notification-preference controls.
- Replace the shared internal token with authenticated service-to-service communication.
