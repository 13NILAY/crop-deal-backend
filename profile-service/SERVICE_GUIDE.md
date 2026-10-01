# Profile Service Guide

Profile Service replaces the separate Farmer and Dealer CRUD services.

## Runtime
- Port: `8085`
- Database: `cropdeal_profile`
- Eureka name: `PROFILE-SERVICE`

## Ownership
Authentication Service owns users, passwords, and roles. Profile Service owns optional Farmer and Dealer profiles. Payment Service owns bank accounts.

One user can have a Farmer profile, a Dealer profile, both, or neither. Profiles are linked to the authenticated user email and are protected by role and ownership checks.

## APIs
Farmer:
- `POST /farmers`
- `GET /farmers`
- `GET /farmers/me`
- `GET /farmers/{id}`
- `PUT /farmers/{id}`
- `DELETE /farmers/{id}`

Dealer:
- `POST /dealers`
- `GET /dealers`
- `GET /dealers/{id}`
- `PUT /dealers/{id}`
- `DELETE /dealers/{id}`

The Gateway keeps the same `/farmers/**` and `/dealers/**` paths, but both now route to Profile Service.
