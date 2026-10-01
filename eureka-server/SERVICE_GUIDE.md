# Eureka Server Guide

## Purpose
Eureka Server is CropDeal's service registry. It keeps a list of running services and their network locations so clients can discover services by name instead of hardcoding host and port values.

## Runtime
- Port: `8761`
- Application name: `eureka-server`
- Main class: `EurekaServerApplication`
- Database: none

Eureka does not register itself and does not fetch a registry. It is the registry that other services contact.

## Flow
1. Eureka starts first.
2. Each service starts with `eureka.client.service-url.defaultZone=http://localhost:8761/eureka/`.
3. The service registers its application name and address.
4. API Gateway and load-balanced clients find services by names such as `CROP-SERVICE`.

## What it owns
- Service registration
- Service discovery metadata
- Health/availability information

## What it does not own
- Users
- Crops
- Orders
- Payments
- Business data

## Startup
Start Eureka before all Eureka clients. Open `http://localhost:8761` to view the registry dashboard.
