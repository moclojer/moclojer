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
- **Configuration** - YAML, EDN, OpenAPI, and Postman Collection formats
- **Endpoints** - HTTP methods, paths, and responses
- **Templates** - Dynamic content generation
- **Parameters** - Path, query, body, and header handling

### ⚡ Advanced Features
Specialized functionality for complex scenarios:
- WebSocket support
- External bodies
- Webhooks
- Rate limiting
- Multi-domain support

### 🔧 Framework Integration
Using moclojer as a library and integrating with applications:
- Clojure integration
- Testing workflows
- Development practices

### 📖 Reference Documentation
Complete technical reference for all features:
- Configuration specification
- Template variables
- CLI reference
- FAQ and troubleshooting

### 🚀 Deployment & Operations
Production deployment and operational concerns:
- Docker deployment
- Cloud deployment
- Monitoring and security

### 💡 Examples & Recipes
Practical examples and common patterns:
- REST API mocking
- Industry-specific examples
- Integration examples

## 🎯 Learning Paths

### I'm entirely new to moclojer
1. Read the [Overview](getting-started/overview.md)
2. Follow the [Installation](getting-started/installation.md) guide
3. Complete all tutorials in the "First Steps" section
4. Explore [Core Concepts](topics/) as needed

### I want to mock a specific type of API
1. Check [Examples & Recipes](examples/) for your use case
2. Review relevant [Core Concepts](topics/)
3. Refer to the [Configuration Specification](reference/configuration-spec.md)

### I'm integrating moclojer into my application
1. Read [Using as a Library](framework/using-as-library.md)
2. Check [Testing Integration](framework/testing-integration.md)
3. Review [Development Workflows](framework/development-workflows.md)

### I need help with a specific feature
1. Check the [FAQ](reference/faq.md) first
2. Search the [Reference Documentation](reference/)
3. Look for examples in [Examples & Recipes](examples/)

## 🔍 Quick Reference

### Common Tasks
- **Create your first mock**: [Your First Mock Server](getting-started/your-first-mock.md)
- **Use path parameters**: [Dynamic Responses](getting-started/dynamic-responses.md#step-2-path-parameters)
- **Handle JSON data**: [Template Variables](topics/templates/template-variables.md#json-body-parameters)
- **Mock WebSockets**: [WebSocket Support](advanced/websocket-support.md)
- **Load external data**: [External Bodies](advanced/external-bodies.md)

### Configuration Reference
- **All template variables**: [Template Variables Reference](topics/templates/template-variables.md)
- **Complete YAML specification**: [Configuration Specification](reference/configuration-spec.md)
- **CLI options**: [CLI Reference](reference/cli-reference.md)

### Troubleshooting
- **Common issues**: [FAQ](reference/faq.md)
- **Detailed troubleshooting**: [Troubleshooting Guide](reference/troubleshooting.md)

## 🏃‍♂️ Running Documentation Locally

You can run this documentation locally using the included server:

```bash
# Navigate to the moclojer directory
cd moclojer

# Start the documentation server
node serve-docs.js

# Open http://localhost:3000 in your browser
```

The local server provides:
- Full navigation
- Search functionality
- Mobile-responsive design
- Fast loading

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
3. Test locally using `node serve-docs.js`
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