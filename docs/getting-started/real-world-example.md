---
description: >-
  Build a complete e-commerce API from scratch using everything you've learned.
  This real-world example showcases advanced features, best practices, and complex scenarios.
---

# Real-World Example: E-commerce API

In this final tutorial, you'll build a complete e-commerce API that demonstrates all the features and best practices you've learned. This example showcases how to structure a complex, real-world API with multiple related resources, authentication simulation, and advanced features.

## What you'll learn

- How to design a complete API from scratch
- Managing complex relationships between resources
- Implementing authentication and authorization patterns
- Advanced error handling and validation
- Real-world business logic simulation
- API versioning and documentation
- Performance considerations

## What you'll build

A complete e-commerce API with these features:
- **Product catalog** with categories and search
- **User management** with different roles
- **Shopping cart** functionality
- **Order management** with status tracking
- **Authentication** simulation
- **Admin endpoints** for management
- **Comprehensive error handling**

## API Overview

### Core Resources
- **Products** - Product catalog management
- **Categories** - Product categorization
- **Users** - Customer and admin accounts
- **Cart** - Shopping cart functionality
- **Orders** - Order processing and tracking
- **Reviews** - Product reviews and ratings

### API Structure
```
/api/v1/
├── auth/
│   ├── login
│   ├── register
│   └── profile
├── products/
│   ├── {id}
│   ├── search
│   └── {id}/reviews
├── categories/
├── cart/
│   ├── items
│   └── checkout
├── orders/
│   ├── {id}
│   └── {id}/status
└── admin/
    ├── products
    ├── orders
    └── users
```

## Step 1: Authentication endpoints

Start with user authentication and profile management:

```yaml
# === AUTHENTICATION ===

# User registration
- endpoint:
    method: POST
    path: /api/v1/auth/register
    response:
      status: 201
      headers:
        Content-Type: application/json
      body: >
        {
          "user": {
            "id": 12345,
            "email": "{{json-params.email}}",
            "name": "{{json-params.name}}",
            "role": "customer",
            "created_at": "2024-01-15T10:30:00Z"
          },
          "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
          "expires_in": 3600,
          "message": "Registration successful"
        }

# User login
- endpoint:
    method: POST
    path: /api/v1/auth/login
    response:
      status: 200
      headers:
        Content-Type: application/json
      body: >
        {
          "user": {
            "id": 12345,
            "email": "{{json-params.email}}",
            "name": "John Doe",
            "role": "customer",
            "last_login": "2024-01-15T10:30:00Z"
          },
          "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
          "expires_in": 3600,
          "message": "Login successful"
        }

# Get user profile
- endpoint:
    method: GET
    path: /api/v1/auth/profile
    response:
      status: 200
      headers:
        Content-Type: application/json
      body: >
        {
          "id": 12345,
          "email": "{{header-params.X-User-Email}}",
          "name": "{{header-params.X-User-Name}}",
          "role": "{{header-params.X-User-Role}}",
          "profile": {
            "phone": "+1-555-0123",
            "address": {
              "street": "123 Main St",
              "city": "Anytown",
              "state": "ST",
              "zip": "12345",
              "country": "US"
            },
            "preferences": {
              "newsletter": true,
              "notifications": true
            }
          },
          "stats": {
            "orders_count": 5,
            "total_spent": "299.97",
            "member_since": "2024-01-01T00:00:00Z"
          }
        }

# Authentication error
- endpoint:
    method: POST
    path: /api/v1/auth/invalid
    response:
      status: 401
      headers:
        Content-Type: application/json
      body: >
        {
          "error": "Unauthorized",
          "message": "Invalid email or password",
          "code": "INVALID_CREDENTIALS",
          "timestamp": "2024-01-15T10:30:00Z"
        }
```

## Step 2: Product catalog

Create a comprehensive product catalog with categories:

```yaml
# === PRODUCTS ===

# Get all products with filters
- endpoint:
    method: GET
    path: /api/v1/products
    response:
      status: 200
      headers:
        Content-Type: application/json
      body: >
        {
          "products": [
            {
              "id": 1,
              "name": "Wireless Bluetooth Headphones",
              "description": "High-quality wireless headphones with noise cancellation",
              "price": 99.99,
              "sale_price": 79.99,
              "category": "Electronics",
              "category_id": 1,
              "brand": "AudioTech",
              "sku": "AT-WH-001",
              "stock": 45,
              "rating": 4.5,
              "reviews_count": 128,
              "images": [
                "https://api.example.com/images/headphones-1.jpg",
                "https://api.example.com/images/headphones-2.jpg"
              ],
              "tags": ["wireless", "bluetooth", "noise-cancellation"],
              "created_at": "2024-01-01T00:00:00Z"
            },
            {
              "id": 2,
              "name": "Organic Cotton T-Shirt",
              "description": "Comfortable organic cotton t-shirt in various colors",
              "price": 24.99,
              "category": "Clothing",
              "category_id": 2,
              "brand": "EcoWear",
              "sku": "EW-TS-001",
              "stock": 120,
              "rating": 4.2,
              "reviews_count": 89,
              "variants": [
                {"size": "S", "color": "Blue", "stock": 30},
                {"size": "M", "color": "Blue", "stock": 35},
                {"size": "L", "color": "Blue", "stock": 25}
              ],
              "images": ["https://api.example.com/images/tshirt-blue.jpg"],
              "tags": ["organic", "cotton", "eco-friendly"],
              "created_at": "2024-01-02T00:00:00Z"
            }
          ],
          "pagination": {
            "page": {{query-params.page}},
            "per_page": {{query-params.per_page}},
            "total": 156,
            "total_pages": 16
          },
          "filters": {
            "category": "{{query-params.category}}",
            "min_price": "{{query-params.min_price}}",
            "max_price": "{{query-params.max_price}}",
            "brand": "{{query-params.brand}}",
            "sort": "{{query-params.sort}}"
          }
        }

# Get specific product
- endpoint:
    method: GET
    path: /api/v1/products/:id
    response:
      status: 200
      headers:
        Content-Type: application/json
      body: >
        {
          "id": {{path-params.id}},
          "name": "Product {{path-params.id}}",
          "description": "Detailed description for product {{path-params.id}}",
          "price": 99.99,
          "sale_price": 79.99,
          "category": "Electronics",
          "category_id": 1,
          "brand": "TechBrand",
          "sku": "TB-{{path-params.id}}-001",
          "stock": 25,
          "rating": 4.3,
          "reviews_count": 45,
          "specifications": {
            "weight": "250g",
            "dimensions": "15cm x 10cm x 5cm",
            "color": "Black",
            "material": "Plastic/Metal"
          },
          "images": [
            "https://api.example.com/images/product-{{path-params.id}}-1.jpg",
            "https://api.example.com/images/product-{{path-params.id}}-2.jpg"
          ],
          "related_products": [2, 3, 4],
          "tags": ["electronics", "popular", "featured"],
          "created_at": "2024-01-01T00:00:00Z",
          "updated_at": "2024-01-15T10:30:00Z"
        }

# Search products
- endpoint:
    method: GET
    path: /api/v1/products/search
    response:
      status: 200
      headers:
        Content-Type: application/json
      body: >
        {
          "query": "{{query-params.q}}",
          "results": [
            {
              "id": 1,
              "name": "Search result for '{{query-params.q}}'",
              "description": "Product matching your search for {{query-params.q}}",
              "price": 59.99,
              "category": "Electronics",
              "rating": 4.4,
              "image": "https://api.example.com/images/search-result.jpg"
            }
          ],
          "total_results": 12,
          "search_time": "0.045s",
          "suggestions": ["{{query-params.q}} accessories", "{{query-params.q}} reviews"],
          "filters": {
            "category": "{{query-params.category}}",
            "price_range": "{{query-params.price_range}}"
          }
        }

# === CATEGORIES ===

# Get all categories
- endpoint:
    method: GET
    path: /api/v1/categories
    response:
      status: 200
      headers:
        Content-Type: application/json
      body: >
        {
          "categories": [
            {
              "id": 1,
              "name": "Electronics",
              "slug": "electronics",
              "description": "Electronic devices and gadgets",
              "product_count": 89,
              "image": "https://api.example.com/images/category-electronics.jpg",
              "subcategories": [
                {"id": 11, "name": "Smartphones", "product_count": 25},
                {"id": 12, "name": "Laptops", "product_count": 18},
                {"id": 13, "name": "Audio", "product_count": 31}
              ]
            },
            {
              "id": 2,
              "name": "Clothing",
              "slug": "clothing",
              "description": "Fashion and apparel",
              "product_count": 67,
              "image": "https://api.example.com/images/category-clothing.jpg",
              "subcategories": [
                {"id": 21, "name": "T-Shirts", "product_count": 22},
                {"id": 22, "name": "Jeans", "product_count": 15},
                {"id": 23, "name": "Accessories", "product_count": 30}
              ]
            }
          ]
        }
```

## Step 3: Shopping cart

Implement shopping cart functionality:

```yaml
# === SHOPPING CART ===

# Get cart contents
- endpoint:
    method: GET
    path: /api/v1/cart
    response:
      status: 200
      headers:
        Content-Type: application/json
      body: >
        {
          "user_id": "{{header-params.X-User-ID}}",
          "items": [
            {
              "id": 1,
              "product_id": 101,
              "product_name": "Wireless Headphones",
              "product_image": "https://api.example.com/images/headphones.jpg",
              "price": 99.99,
              "sale_price": 79.99,
              "quantity": 2,
              "subtotal": 159.98,
              "added_at": "2024-01-15T09:00:00Z"
            },
            {
              "id": 2,
              "product_id": 205,
              "product_name": "Cotton T-Shirt",
              "product_image": "https://api.example.com/images/tshirt.jpg",
              "price": 24.99,
              "quantity": 1,
              "subtotal": 24.99,
              "variant": {"size": "M", "color": "Blue"},
              "added_at": "2024-01-15T10:15:00Z"
            }
          ],
          "summary": {
            "items_count": 3,
            "subtotal": 184.97,
            "tax": 14.80,
            "shipping": 9.99,
            "discount": 0.00,
            "total": 209.76
          },
          "updated_at": "2024-01-15T10:30:00Z"
        }

# Add item to cart
- endpoint:
    method: POST
    path: /api/v1/cart/items
    response:
      status: 201
      headers:
        Content-Type: application/json
      body: >
        {
          "item": {
            "id": 12345,
            "product_id": {{json-params.product_id}},
            "product_name": "Product {{json-params.product_id}}",
            "price": 99.99,
            "quantity": {{json-params.quantity}},
            "subtotal": {{json-params.quantity}} * 99.99,
            "added_at": "2024-01-15T10:30:00Z"
          },
          "cart_summary": {
            "items_count": 4,
            "total": 309.75
          },
          "message": "Item added to cart successfully"
        }

# Update cart item
- endpoint:
    method: PUT
    path: /api/v1/cart/items/:id
    response:
      status: 200
      headers:
        Content-Type: application/json
      body: >
        {
          "item": {
            "id": {{path-params.id}},
            "product_id": 101,
            "quantity": {{json-params.quantity}},
            "subtotal": {{json-params.quantity}} * 99.99,
            "updated_at": "2024-01-15T10:30:00Z"
          },
          "cart_summary": {
            "items_count": 3,
            "total": 284.76
          },
          "message": "Cart item updated successfully"
        }

# Remove item from cart
- endpoint:
    method: DELETE
    path: /api/v1/cart/items/:id
    response:
      status: 200
      headers:
        Content-Type: application/json
      body: >
        {
          "removed_item_id": {{path-params.id}},
          "cart_summary": {
            "items_count": 2,
            "total": 184.97
          },
          "message": "Item removed from cart successfully"
        }

# Checkout (create order from cart)
- endpoint:
    method: POST
    path: /api/v1/cart/checkout
    response:
      status: 201
      headers:
        Content-Type: application/json
        Location: /api/v1/orders/ORD-{{json-params.user_id}}-12345
      body: >
        {
          "order": {
            "id": "ORD-{{json-params.user_id}}-12345",
            "user_id": "{{json-params.user_id}}",
            "status": "pending",
            "items": [
              {
                "product_id": 101,
                "product_name": "Wireless Headphones",
                "quantity": 2,
                "price": 79.99,
                "subtotal": 159.98
              }
            ],
            "shipping_address": {
              "name": "{{json-params.shipping.name}}",
              "street": "{{json-params.shipping.street}}",
              "city": "{{json-params.shipping.city}}",
              "zip": "{{json-params.shipping.zip}}"
            },
            "payment": {
              "method": "{{json-params.payment.method}}",
              "status": "pending"
            },
            "totals": {
              "subtotal": 159.98,
              "tax": 12.80,
              "shipping": 9.99,
              "total": 182.77
            },
            "created_at": "2024-01-15T10:30:00Z",
            "estimated_delivery": "2024-01-20T00:00:00Z"
          },
          "message": "Order created successfully"
        }
```

## Step 4: Order management

Complete order processing and tracking:

```yaml
# === ORDERS ===

# Get user orders
- endpoint:
    method: GET
    path: /api/v1/orders
    response:
      status: 200
      headers:
        Content-Type: application/json
      body: >
        {
          "orders": [
            {
              "id": "ORD-12345-001",
              "status": "delivered",
              "items_count": 2,
              "total": 182.77,
              "created_at": "2024-01-10T14:30:00Z",
              "delivered_at": "2024-01-13T16:45:00Z"
            },
            {
              "id": "ORD-12345-002",
              "status": "shipped",
              "items_count": 1,
              "total": 99.99,
              "created_at": "2024-01-12T09:15:00Z",
              "tracking_number": "TRK123456789"
            },
            {
              "id": "ORD-12345-003",
              "status": "processing",
              "items_count": 3,
              "total": 249.97,
              "created_at": "2024-01-15T10:30:00Z",
              "estimated_ship_date": "2024-01-17T00:00:00Z"
            }
          ],
          "pagination": {
            "page": {{query-params.page}},
            "per_page": 10,
            "total": 15
          },
          "filters": {
            "status": "{{query-params.status}}",
            "date_from": "{{query-params.date_from}}",
            "date_to": "{{query-params.date_to}}"
          }
        }

# Get specific order
- endpoint:
    method: GET
    path: /api/v1/orders/:id
    response:
      status: 200
      headers:
        Content-Type: application/json
      body: >
        {
          "id": "{{path-params.id}}",
          "user_id": 12345,
          "status": "shipped",
          "items": [
            {
              "product_id": 101,
              "product_name": "Wireless Headphones",
              "product_image": "https://api.example.com/images/headphones.jpg",
              "quantity": 1,
              "price": 79.99,
              "subtotal": 79.99
            },
            {
              "product_id": 205,
              "product_name": "Cotton T-Shirt",
              "product_image": "https://api.example.com/images/tshirt.jpg",
              "quantity": 2,
              "price": 24.99,
              "subtotal": 49.98,
              "variant": {"size": "M", "color": "Blue"}
            }
          ],
          "shipping_address": {
            "name": "John Doe",
            "street": "123 Main St",
            "city": "Anytown",
            "state": "ST",
            "zip": "12345",
            "country": "US"
          },
          "billing_address": {
            "name": "John Doe",
            "street": "123 Main St",
            "city": "Anytown",
            "state": "ST",
            "zip": "12345",
            "country": "US"
          },
          "payment": {
            "method": "credit_card",
            "last_four": "****1234",
            "status": "paid",
            "transaction_id": "TXN-ABC123"
          },
          "totals": {
            "subtotal": 129.97,
            "tax": 10.40,
            "shipping": 9.99,
            "discount": 5.00,
            "total": 145.36
          },
          "tracking": {
            "number": "TRK{{path-params.id}}789",
            "carrier": "FastShip",
            "url": "https://tracking.fastship.com/TRK{{path-params.id}}789"
          },
          "timeline": [
            {
              "status": "placed",
              "timestamp": "2024-01-12T09:15:00Z",
              "note": "Order placed successfully"
            },
            {
              "status": "confirmed",
              "timestamp": "2024-01-12T09:30:00Z",
              "note": "Payment confirmed"
            },
            {
              "status": "processing",
              "timestamp": "2024-01-12T14:00:00Z",
              "note": "Order is being prepared"
            },
            {
              "status": "shipped",
              "timestamp": "2024-01-13T08:00:00Z",
              "note": "Order shipped with tracking number TRK{{path-params.id}}789"
            }
          ],
          "created_at": "2024-01-12T09:15:00Z",
          "updated_at": "2024-01-13T08:00:00Z",
          "estimated_delivery": "2024-01-16T17:00:00Z"
        }

# Cancel order
- endpoint:
    method: POST
    path: /api/v1/orders/:id/cancel
    response:
      status: 200
      headers:
        Content-Type: application/json
      body: >
        {
          "order_id": "{{path-params.id}}",
          "previous_status": "processing",
          "new_status": "cancelled",
          "refund": {
            "amount": 145.36,
            "method": "original_payment",
            "estimated_days": "3-5 business days"
          },
          "cancelled_at": "2024-01-15T10:30:00Z",
          "message": "Order cancelled successfully. Refund will be processed within 3-5 business days."
        }
```

## Step 5: Product reviews

Add review functionality:

```yaml
# === PRODUCT REVIEWS ===

# Get product reviews
- endpoint:
    method: GET
    path: /api/v1/products/:id/reviews
    response:
      status: 200
      headers:
        Content-Type: application/json
      body: >
        {
          "product_id": {{path-params.id}},
          "reviews": [
            {
              "id": 1,
              "user_name": "Sarah M.",
              "user_verified": true,
              "rating": 5,
              "title": "Excellent product!",
              "comment": "Really happy with this purchase. Great quality and fast shipping.",
              "helpful_votes": 12,
              "created_at": "2024-01-10T14:30:00Z",
              "verified_purchase": true
            },
            {
              "id": 2,
              "user_name": "Mike R.",
              "user_verified": true,
              "rating": 4,
              "title": "Good value for money",
              "comment": "Works as expected, though delivery took a bit longer than anticipated.",
              "helpful_votes": 8,
              "created_at": "2024-01-08T09:15:00Z",
              "verified_purchase": true
            }
          ],
          "summary": {
            "average_rating": 4.3,
            "total_reviews": 45,
            "rating_distribution": {
              "5": 22,
              "4": 15,
              "3": 5,
              "2": 2,
              "1": 1
            }
          },
          "pagination": {
            "page": {{query-params.page}},
            "per_page": 10,
            "total": 45
          },
          "filters": {
            "rating": "{{query-params.rating}}",
            "verified_only": "{{query-params.verified_only}}"
          }
        }

# Add product review
- endpoint:
    method: POST
    path: /api/v1/products/:id/reviews
    response:
      status: 201
      headers:
        Content-Type: application/json
      body: >
        {
          "review": {
            "id": 12345,
            "product_id": {{path-params.id}},
            "user_id": "{{header-params.X-User-ID}}",
            "user_name": "{{header-params.X-User-Name}}",
            "rating": {{json-params.rating}},
            "title": "{{json-params.title}}",
            "comment": "{{json-params.comment}}",
            "verified_purchase": true,
            "created_at": "2024-01-15T10:30:00Z"
          },
          "updated_product_rating": 4.4,
          "message": "Review added successfully"
        }
```

## Step 6: Admin endpoints

Add administrative functionality:

```yaml
# === ADMIN ENDPOINTS ===

# Admin dashboard stats
- endpoint:
    method: GET
    path: /api/v1/admin/dashboard
    response:
      status: 200
      headers:
        Content-Type: application/json
      body: >
        {
          "stats": {
            "total_products": 156,
            "total_orders": 1247,
            "total_users": 3421,
            "total_revenue": "156,789.45"
          },
          "recent_orders": [
            {
              "id": "ORD-12345-001",
              "customer": "John Doe",
              "total": 182.77,
              "status": "pending",
              "created_at": "2024-01-15T10:30:00Z"
            }
          ],
          "low_stock_products": [
            {
              "id": 101,
              "name": "Wireless Headphones",
              "current_stock": 3,
              "minimum_stock": 10
            }
          ],
          "top_products": [
            {
              "id": 205,
              "name": "Cotton T-Shirt",
              "sales_count": 89,
              "revenue": "2,223.11"
            }
          ]
        }

# Admin manage products
- endpoint:
    method: GET
    path: /api/v1/admin/products
    response:
      status: 200
      headers:
        Content-Type: application/json
      body: >
        {
          "products": [
            {
              "id": 1,
              "name": "Wireless Headphones",
              "sku": "WH-001",
              "price": 99.99,
              "stock": 45,
              "category": "Electronics",
              "status": "active",
              "created_at": "2024-01-01T00:00:00Z",
              "sales_count": 23,
              "revenue": "2,299.77"
            }
          ],
          "pagination": {
            "page": {{query-params.page}},
            "per_page": 25,
            "total": 156
          },
          "filters": {
            "category": "{{query-params.category}}",
            "status": "{{query-params.status}}",
            "low_stock": "{{query-params.low_stock}}"
          }
        }

# Admin manage orders
- endpoint:
    method: GET
    path: /api/v1/admin/orders
    response:
      status: 200
      headers:
        Content-Type: application/json
      body: >
        {
          "orders": [
            {
              "id": "ORD-12345-001",
              "customer": {
                "id": 12345,
                "name": "John Doe",
                "email": "john@example.com"
              },
              "status": "{{query-params.status}}",
              "items_count": 2,
              "total": 182.77,
              "created_at": "2024-01-15T10:30:00Z",
              "payment_status": "paid"
            }
          ],
          "pagination": {
            "page": {{query-params.page}},
            "per_page": 25,
            "total": 1247
          },
          "filters": {
            "status": "{{query-params.status}}",
            "date_from": "{{query-params.date_from}}",
            "payment_status": "{{query-params.payment_status}}"
          }
        }

# Update order status
- endpoint:
    method: PATCH
    path: /api/v1/admin/orders/:id/status
    response:
      status: 200
      headers:
        Content-Type: application/json
      body: >
        {
          "order_id": "{{path-params.id}}",
          "previous_status": "processing",
          "new_status": "{{json-params.status}}",
          "updated_by": "{{header-params.X-Admin-ID}}",
          "updated_at": "2024-01-15T10:30:00Z",
          "notification_sent": true,
          "message": "Order status updated successfully"
        }
```

## Step 7: Error handling

Add comprehensive error responses:

```yaml
# === ERROR HANDLING ===

# Product not found
- endpoint:
    method: GET
    path: /api/v1/products/99999
    response:
      status: 404
      headers:
        Content-Type: application/json
      body: >
        {
          "error": "Not Found",
          "message": "Product with ID 99999 not found",
          "code": "PRODUCT_NOT_FOUND",
          "timestamp": "2024-01-15T10:30:00Z",
          "request_id": "req_abc123def456"
        }

# Insufficient stock
- endpoint:
    method: POST
    path: /api/v1/cart/items/out-of-stock
    response:
      status: 422
      headers:
        Content-Type: application/json
      body: >
        {
          "error": "Unprocessable Entity",