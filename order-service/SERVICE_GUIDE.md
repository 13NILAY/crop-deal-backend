# Order Service Guide

## Purpose
Order Service owns the order aggregate and lifecycle. It connects a dealer to a crop and farmer without directly opening another service's database.

## Runtime
- Port: `8087`
- Database: `cropdeal_order`
- Main API prefix: `/orders`
- Communication: Eureka/load-balanced REST and RabbitMQ

## Main APIs
- `POST /orders`: create an order
- `GET /orders/{id}`: read an order
- `GET /orders/dealer/me`: dealer order history
- `GET /orders/farmer/{farmerId}`: farmer order history
- `PUT /orders/{id}/status?status=...`: update lifecycle status

## Create-order flow
1. Authenticated Dealer sends crop ID, farmer ID, and quantity.
2. Order Service calls Crop Service through Eureka.
3. Crop Service atomically reserves the quantity and returns the current price.
4. Order Service stores a price snapshot and creates status `CREATED`.
5. Order Service publishes `OrderCreatedEvent` to `cropdeal.orders`.
6. Payment Service consumes the event and creates a Razorpay order.

## Payment flow
Payment Service publishes payment status events. Order Service consumes them:
- `SUCCESS` -> `CONFIRMED`
- `FAILED` -> `CANCELLED`

## Important rule
Order Service owns order state. Crop, Dealer, Farmer, and Payment databases remain separate.
