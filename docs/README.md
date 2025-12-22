# Moclojer Documentation

Welcome to the complete documentation for moclojer - a simple and efficient HTTP mock server. This documentation is designed to help you get started quickly and become proficient with all of moclojer's features.

## 🚀 Getting Started

New to moclojer? Start with our progressive tutorial series:

1. **[Overview](getting-started/overview.md)** - Learn what moclojer is and why you'd want to use it
2. **[Installation](getting-started/installation.md)** - Get moclojer running on your system
3. **[Using Postman Collections](getting-started/postman-collections.md)** - Use your existing Postman Collections directly
4. **[Your First Mock Server](getting-started/your-first-mock.md)** - Create a working API in 10 minutes
5. **[Dynamic Responses](getting-started/dynamic-responses.md)** - Make your mocks respond to request data
6. **[Multiple Endpoints](getting-started/multiple-endpoints.md)** - Build complete APIs with proper structure
7. **[Real-World Example](getting-started/real-world-example.md)** - Complete e-commerce API tutorial

## 📚 Documentation Structure

This documentation follows a progressive structure inspired by Django's excellent documentation:

### 🎯 First Steps

Perfect for beginners - get up and running quickly with guided tutorials.

### 🧠 Core Concepts

Understand how moclojer works with detailed explanations of key concepts:

- **Configuration Formats**
  - [YAML Format](topics/configuration/yaml-format.md) - Complete syntax and best practices
  - [OpenAPI Format](topics/configuration/openapi-format.md) - Import OpenAPI 3.x specifications
  - [Postman Collection Format](topics/configuration/postman-format.md) - Use Postman Collections v2.1
  - [EDN Format](topics/configuration/edn-format.md) - Native Clojure configuration
- **Endpoints**
  - [HTTP Methods](topics/endpoints/http-methods.md) - GET, POST, PUT, DELETE, PATCH, HEAD, OPTIONS
  - [Path Patterns](topics/endpoints/path-patterns.md) - Route patterns and matching
  - [Response Structure](topics/endpoints/response-structure.md) - Building HTTP responses
  - [Request Matching](topics/request-matching.md) - How moclojer routes requests
- **Templates**
  - [Template System](topics/templates/template-system.md) - Dynamic content generation
  - [Template Variables](topics/templates/template-variables.md) - Available variables
- **Parameters**
  - [Path Parameters](topics/parameters/path-parameters.md) - Dynamic URL parameters
  - [Query Parameters](topics/parameters/query-parameters.md) - Filters and pagination
  - [Body Parameters](topics/parameters/body-parameters.md) - Request body data
  - [Header Parameters](topics/parameters/header-parameters.md) - HTTP headers

### 🔧 How-to Guides

Practical recipes for common use cases:

- **Testing**
  - [Integration Testing](how-to/testing/integration-testing.md) - Test with mock APIs
  - [E2E Testing](how-to/testing/e2e-testing.md) - Cypress and Playwright integration
  - [Contract Testing](how-to/testing/contract-testing.md) - API contract validation
- **Deployment**
  - [Docker](how-to/deployment/docker.md) - Docker and Docker Compose
  - [Kubernetes](how-to/deployment/kubernetes.md) - K8s deployment
  - [Cloud Run](how-to/deployment/cloud-run.md) - Google Cloud Run
- **Common Patterns**
  - [CRUD Operations](how-to/patterns/crud-operations.md) - Create, Read, Update, Delete
  - [Pagination](how-to/patterns/pagination.md) - Offset/Limit and cursor-based
  - [Authentication Mock](how-to/patterns/authentication-mock.md) - JWT, OAuth, API keys
  - [Error Handling](how-to/patterns/error-handling.md) - Standard error responses
  - [API Versioning](how-to/patterns/api-versioning.md) - Version strategies
  - [CORS Configuration](how-to/patterns/cors.md) - Cross-origin requests

### ⚡ Advanced Features

Specialized functionality for complex scenarios:

- [WebSocket Support](advanced/websocket-support.md) - Real-time bidirectional communication
- [External Bodies](advanced/external-bodies.md) - Load responses from JSON/Excel files
- [Webhook Integration](advanced/webhook-integration.md) - Background requests
- [Rate Limiting](advanced/rate-limiting.md) - Request throttling
- [Multi-Domain Support](advanced/multi-domain-support.md) - Host-based routing

### 🏗️ Framework Integration

Using moclojer as a library and integrating with applications:

- [Using as a Library](framework/using-as-library.md) - Programmatic usage

### 📖 Reference Documentation

Complete technical reference for all features:

- [Configuration Specification](reference/configuration-spec.md) - Complete YAML spec
- [CLI Reference](reference/cli-reference.md) - Command-line options
- [FAQ](reference/faq.md) - Frequently asked questions
- [Troubleshooting](reference/troubleshooting.md) - Common issues and solutions

### 💡 Examples & Use Cases

Real-world examples and complete implementations:

- [Examples Overview](examples/README.md) - Guide to all examples
- **REST APIs**
  - [Basic CRUD API](examples/rest-api/basic-crud.md) - Task management system
  - [Blog API](examples/rest-api/blog-api.md) - Blog with nested resources
- **Third-Party API Mocks**
  - [Stripe Mock](examples/third-party/stripe-mock.md) - Payment API simulation

## 🎯 Learning Paths

### I'm entirely new to moclojer

1. Read the [Overview](getting-started/overview.md)
2. Follow the [Installation](getting-started/installation.md) guide
3. Complete all tutorials in the "First Steps" section
4. Explore [Core Concepts](topics/) as needed

### I want to mock a specific type of API

1. Start with [CRUD Operations How-to](how-to/patterns/crud-operations.md)
2. Check [Examples & Use Cases](examples/) for your use case
3. Review [HTTP Methods](topics/endpoints/http-methods.md)
4. Learn about [Path](topics/parameters/path-parameters.md) and [Query Parameters](topics/parameters/query-parameters.md)
5. Refer to the [Configuration Specification](reference/configuration-spec.md)

### I'm integrating moclojer into my application

1. Read [Using as a Library](framework/using-as-library.md)
2. Check [Integration Testing](how-to/testing/integration-testing.md)
3. Review deployment guides in [How-to Guides](how-to/deployment/)

### I need help with a specific feature

1. Check the [Troubleshooting Guide](reference/troubleshooting.md) first
2. Review the [FAQ](reference/faq.md)
3. Search the [Reference Documentation](reference/)
4. Check [CLI Reference](reference/cli-reference.md) for command-line options
5. Look for examples in [How-to Guides](how-to/)

## 🔍 Quick Reference

### Common Tasks

- **Create your first mock**: [Your First Mock Server](getting-started/your-first-mock.md)
- **Use path parameters**: [Dynamic Responses](getting-started/dynamic-responses.md)
- **Handle JSON data**: [Body Parameters](topics/parameters/body-parameters.md)
- **Mock WebSockets**: [WebSocket Support](advanced/websocket-support.md)
- **Load external data**: [External Bodies](advanced/external-bodies.md)

### Configuration Reference

- **All template variables**: [Template Variables Reference](topics/templates/template-variables.md)
- **Complete YAML specification**: [Configuration Specification](reference/configuration-spec.md)
- **CLI options**: [CLI Reference](reference/cli-reference.md)

### Troubleshooting

- **Common issues**: [FAQ](reference/faq.md)
- **Detailed troubleshooting**: [Troubleshooting Guide](reference/troubleshooting.md)

## 🤝 Contributing to Documentation

We welcome contributions to improve the documentation! Here's how you can help:

### Quick Fixes

- Fix typos or broken links
- Improve unclear explanations
- Add missing examples

### Larger Contributions

- Write new tutorials
- Add industry-specific examples
- Improve existing guides
- Translate content

### How to Contribute

1. Fork the repository
2. Make your changes in the `docs/` directory
3. Test locally to ensure links work
4. Submit a pull request

### Writing Guidelines

- **Use clear, simple language** - Write for developers of all skill levels
- **Include practical examples** - Show, don't just tell
- **Follow the progressive structure** - Start simple, add complexity gradually
- **Test all code examples** - Ensure examples actually work
- **Use proper Markdown formatting** - Follow existing style

## 📞 Getting Help

### Community Resources

- **[GitHub Discussions](https://github.com/moclojer/moclojer/discussions)** - Ask questions and share ideas
- **[GitHub Issues](https://github.com/moclojer/moclojer/issues)** - Report bugs or request features

### Documentation Issues

If you find problems with the documentation:

1. Check if it's already reported in [GitHub Issues](https://github.com/moclojer/moclojer/issues)
2. Create a new issue with:
   - Clear description of the problem
   - Which page/section is affected
   - Suggested improvement (if applicable)

## 📄 Documentation License

This documentation is part of the moclojer project and follows the same MIT license as the main project.

---

**Ready to get started?** Begin with the [Overview](getting-started/overview.md) to learn what moclojer can do for you! 🚀
