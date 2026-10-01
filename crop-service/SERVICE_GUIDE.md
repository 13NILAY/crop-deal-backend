# Crop Service Guide

## Purpose
Crop Service owns crop listings and inventory. It stores crop name, type, quantity, price, location, farmer ID, status, and creation time.

## Runtime
- Port: `8082`
- Database: `cropdeal_crop_db`
- Main API prefix: `/crops`
- Read access: public where configured
- Write/reservation access: authenticated

## Main APIs
- `POST /crops`: create a listing
- `GET /crops`: list crops
- `GET /crops/{id}`: get one crop
- `PUT /crops/{id}`: update a crop
- `DELETE /crops/{id}`: delete a crop
- `POST /crops/{id}/reserve?farmerId=...&quantity=...`: reserve inventory
- Search/filter/page endpoints support marketplace reads.

## Order integration
Order Service does not access this database directly. It calls the reserve endpoint. Crop Service checks farmer ownership, availability, and requested quantity, then decreases inventory atomically. When quantity becomes zero, status becomes `SOLD`.

## Important files
- `model/Crop`: database entity
- `model/CropStatus`: `AVAILABLE`, `SOLD`, `EXPIRED`
- `repository/CropRepository`: persistence
- `service/CropServiceImpl`: business rules
- `controller/CropController`: REST API
- `config/JwtAuthenticationFilter`: JWT validation

## Ownership
Authentication identifies the user. Profile Service owns farmer and dealer profile details. Crop Service owns the crop listing itself.
