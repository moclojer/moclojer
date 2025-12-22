# moclojer Examples

Welcome to the moclojer examples collection! These are complete, production-ready examples that demonstrate real-world use cases and best practices.

## 🎯 What's Included

Each example includes:

- ✅ **Complete configuration** - Ready-to-run YAML files
- ✅ **Architecture diagrams** - Visual understanding with Mermaid
- ✅ **Usage examples** - Curl commands and test scripts
- ✅ **Best practices** - Following RESTful and API design patterns
- ✅ **Related documentation** - Links to learn more

## 📂 Examples by Category

### 🔷 REST APIs

Perfect for learning CRUD operations and API design fundamentals.

| Example | Description | Complexity | Key Concepts |
|---------|-------------|------------|--------------|
| **[Basic CRUD](rest-api/basic-crud.md)** | Task management API | ⭐ Beginner | CRUD, pagination, search, errors |
| **[Blog API](rest-api/blog-api.md)** | Complete blog platform | ⭐⭐ Intermediate | Nested resources, relationships, comments |

**Learn:**

- CRUD operations (Create, Read, Update, Delete)
- Proper HTTP status codes
- Pagination and filtering
- Error handling
- Resource relationships

### 🔶 Third-Party API Mocks

Mock popular external APIs for testing without real API calls.

| Example | Description | Complexity | Use Case |
|---------|-------------|------------|----------|
| **[Stripe Mock](third-party/stripe-mock.md)** | Payment processing | ⭐⭐ Intermediate | Local dev, CI/CD, testing |
| **GitHub API Mock** 📝 | Repository management | ⭐⭐ Intermediate | OAuth, webhooks (Coming Soon) |
| **Slack API Mock** 📝 | Team communication | ⭐⭐ Intermediate | Webhooks, bots (Coming Soon) |

**Learn:**

- Third-party API integration patterns
- Payment flows without real transactions
- Webhook event simulation
- Error scenario testing

### 🔶 Microservices (Coming Soon)

Advanced patterns for microservice architectures.

| Example | Description | Complexity | Key Concepts |
|---------|-------------|------------|--------------|
| **Service Mesh** 📝 | Multi-service communication | ⭐⭐⭐ Advanced | Service discovery, load balancing |
| **Event-Driven** 📝 | Async message processing | ⭐⭐⭐ Advanced | Events, queues, pub/sub |

## 🚀 Quick Start

### 1. Choose an Example

Browse the categories above and pick an example that matches your needs.

### 2. Download Configuration

Each example includes a complete YAML configuration file. Copy the configuration from the example page.

### 3. Run with moclojer

```bash
# Save configuration to a file
cat > example.yml << 'EOF'
# ... paste configuration here ...
EOF

# Start moclojer
moclojer --config example.yml --port 8000

# Test the API
curl http://localhost:8000/health
```

### 4. Explore & Modify

- Test with the provided curl commands
- Run the test scripts
- Modify the configuration to fit your needs
- Add your own endpoints

## 📚 Learning Path

### Beginner → Intermediate → Advanced

1. **Start Simple**: [Basic CRUD](rest-api/basic-crud.md)
   - Learn CRUD fundamentals
   - Understand HTTP methods
   - Practice with curl commands

2. **Add Complexity**: [Blog API](rest-api/blog-api.md)
   - Nested resources (posts → comments)
   - Resource relationships
   - Advanced queries

3. **Real-World Integration**: [Stripe Mock](third-party/stripe-mock.md)
   - Mock external APIs
   - Error scenarios
   - Integration testing

4. **Production Patterns**: [How-to Guides](../how-to/)
   - Pagination strategies
   - Authentication patterns
   - Deployment (Docker, K8s)

## 🎓 What You'll Learn

### API Design Principles

- ✅ **RESTful conventions** - Resource naming, HTTP methods
- ✅ **Status codes** - When to use 200, 201, 400, 404, etc.
- ✅ **Error handling** - Meaningful error messages
- ✅ **Pagination** - Handle large datasets
- ✅ **Filtering & search** - Query parameters

### moclojer Features

- ✅ **Template variables** - Dynamic responses
- ✅ **Path parameters** - `/users/:id` patterns
- ✅ **Query parameters** - Filters and pagination
- ✅ **JSON body handling** - Request data access
- ✅ **Header parameters** - Authentication, CORS

### Testing Strategies

- ✅ **Local development** - No external dependencies
- ✅ **CI/CD integration** - Automated testing
- ✅ **Error simulation** - Test failure scenarios
- ✅ **Frontend development** - Backend not ready yet

## 🔗 Related Documentation

### Core Concepts

- **[YAML Format Guide](../topics/configuration/yaml-format.md)** - Configuration syntax
- **[HTTP Methods](../topics/endpoints/http-methods.md)** - GET, POST, PUT, DELETE
- **[Template Variables](../topics/templates/template-variables.md)** - Dynamic content

### How-to Guides

- **[CRUD Operations](../how-to/patterns/crud-operations.md)** - Detailed CRUD patterns
- **[Pagination](../how-to/patterns/pagination.md)** - Pagination strategies
- **[Docker Deployment](../how-to/deployment/docker.md)** - Production deployment

### Getting Started

- **[Your First Mock Server](../getting-started/your-first-mock.md)** - Beginner tutorial
- **[Dynamic Responses](../getting-started/dynamic-responses.md)** - Template variables
- **[Multiple Endpoints](../getting-started/multiple-endpoints.md)** - API structure

## 💡 Tips & Best Practices

### Do ✅

- **Start with working examples** - Copy and modify instead of starting from scratch
- **Test frequently** - Use curl or the provided test scripts
- **Follow RESTful conventions** - Makes APIs predictable and easy to use
- **Add meaningful errors** - Help users understand what went wrong
- **Include pagination** - Essential for list endpoints

### Don't ❌

- **Don't over-engineer** - Start simple, add complexity as needed
- **Don't skip error handling** - Real APIs fail, your mock should too
- **Don't forget documentation** - Comment your custom modifications
- **Don't use in production** - These are mocks for testing only

## 🤝 Contributing Examples

Have a great example? We'd love to include it!

### What Makes a Good Example

- ✅ **Complete and working** - All code must run without errors
- ✅ **Well-documented** - Clear explanations and comments
- ✅ **Real-world relevant** - Solves actual problems
- ✅ **Best practices** - Demonstrates proper patterns
- ✅ **Test scripts included** - Easy to verify functionality

### How to Contribute

1. **Fork the repository**
2. **Create your example** - Follow existing structure
3. **Test thoroughly** - Ensure everything works
4. **Add diagrams** - Mermaid diagrams are great!
5. **Submit pull request**

## 📞 Need Help?

- **Questions?** Join [GitHub Discussions](https://github.com/moclojer/moclojer/discussions)
- **Issues?** Check the [Troubleshooting Guide](../reference/troubleshooting.md)
- **Found a bug?** Open an [issue](https://github.com/moclojer/moclojer/issues)

---

**Ready to start?** Pick an example above and begin mocking! 🚀
