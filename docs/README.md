# Moclojer Documentation

Welcome to the complete documentation for moclojer - a simple and efficient HTTP mock server. This documentation is designed to help you get started quickly and become proficient with all of moclojer's features.

## ✨ Novidades da Documentação

A documentação foi **completamente reorganizada** seguindo as melhores práticas do Django Docs!

**🆕 13 Novos Documentos de Alta Qualidade:**

**Configuration Formats:**
- ⭐ [YAML Format Guide](topics/configuration/yaml-format.md) - Guia completo de sintaxe YAML
- ⭐ [OpenAPI Format](topics/configuration/openapi-format.md) - Importar specs OpenAPI 3.x
- ⭐ [Postman Collection](topics/configuration/postman-format.md) - Usar Postman Collections v2.1

**Parameters (Completo!):**
- ⭐ [Path Parameters](topics/parameters/path-parameters.md) - Parâmetros dinâmicos em URLs
- ⭐ [Query Parameters](topics/parameters/query-parameters.md) - Filtros, paginação e busca
- ⭐ [Body Parameters](topics/parameters/body-parameters.md) - JSON e dados de requisição
- ⭐ [Header Parameters](topics/parameters/header-parameters.md) - HTTP headers (Authorization, etc.)

**Endpoints:**
- ⭐ [HTTP Methods](topics/endpoints/http-methods.md) - GET, POST, PUT, DELETE, PATCH, HEAD, OPTIONS

**How-to Guides (Receitas Práticas):**
- ⭐ [CRUD Operations](how-to/patterns/crud-operations.md) - Create, Read, Update, Delete completo
- ⭐ [Pagination](how-to/patterns/pagination.md) - Offset/Limit, Cursor-based, Link headers
- ⭐ [Docker Deployment](how-to/deployment/docker.md) - Docker & Docker Compose production-ready

**Reference:**
- ⭐ [CLI Reference](reference/cli-reference.md) - Todas as opções de linha de comando
- ⭐ [Troubleshooting Guide](reference/troubleshooting.md) - Resolução de problemas comuns

**🎯 Novas Seções Criadas:**
- **How-to Guides** - Receitas práticas para casos de uso comuns
- **Examples & Use Cases** - Implementações completas e reais (em breve)

**📊 Progresso:** 13 novos docs + 1 plano estratégico + ~5.000 linhas de conteúdo!

📖 Veja o [Plano Completo de Melhorias](community/documentation-improvement-plan.md) para saber o que vem a seguir!

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

### 🧠 Core Concepts ⭐ EXPANDIDO
Understand how moclojer works with detailed explanations of key concepts:
- **Configuration Formats** ⭐
  - [YAML Format](topics/configuration/yaml-format.md) - Sintaxe completa e boas práticas
  - OpenAPI Format - Importar specs OpenAPI (em breve)
  - Postman Format - Usar Postman Collections (em breve)
  - EDN Format - Para usuários Clojure (em breve)
- **Endpoints**
  - [HTTP Methods](topics/endpoints/http-methods.md) ⭐ - GET, POST, PUT, DELETE, etc.
  - Path Patterns - Padrões de rotas (em breve)
  - Response Structure - Estrutura de respostas (em breve)
- **Templates** - Dynamic content generation
  - [Template System](topics/templates/template-system.md)
  - [Template Variables](topics/templates/template-variables.md)
- **Parameters** ⭐
  - [Path Parameters](topics/parameters/path-parameters.md) - Parâmetros dinâmicos de URL
  - [Query Parameters](topics/parameters/query-parameters.md) - Filtros e paginação
  - Body Parameters - Dados no corpo (em breve)
  - Header Parameters - Headers HTTP (em breve)

### ⚡ Advanced Features
Specialized functionality for complex scenarios:
- WebSocket support
- External bodies
- Webhooks
- Rate limiting
- Multi-domain support

### 🔧 How-to Guides 🆕 NOVA SEÇÃO
Practical recipes for common use cases:
- **Testing**
  - Integration Testing (em breve)
  - E2E Testing (em breve)
  - Contract Testing (em breve)
- **Deployment**
  - Docker (em breve)
  - Kubernetes (em breve)
  - Cloud Run (em breve)
- **Common Patterns** ⭐
  - [CRUD Operations](how-to/patterns/crud-operations.md) - Create, Read, Update, Delete completo
  - Pagination (em breve)
  - Authentication Mock (em breve)
  - Error Handling (em breve)

### ⚡ Advanced Features
Specialized functionality for complex scenarios:
- [WebSocket Support](advanced/websocket-support.md)
- [External Bodies](advanced/external-bodies.md)
- [Webhook Integration](advanced/webhook-integration.md)
- [Rate Limiting](advanced/rate-limiting.md)
- [Multi-Domain Support](advanced/multi-domain-support.md)

### 🏗️ Framework Integration
Using moclojer as a library and integrating with applications:
- [Using as a Library](framework/using-as-library.md)
- Testing Integration (em breve)
- Development Workflows (em breve)

### 📖 Reference Documentation ⭐ EXPANDIDO
Complete technical reference for all features:
- [Configuration Specification](reference/configuration-spec.md)
- [CLI Reference](reference/cli-reference.md) ⭐ - Todas as opções de linha de comando
- [Troubleshooting Guide](reference/troubleshooting.md) ⭐ - Resolução de problemas
- Template Variables Reference (em breve)
- Environment Variables (em breve)
- [FAQ](reference/faq.md)

### 💡 Examples & Use Cases 🆕 NOVA SEÇÃO
Real-world examples and complete implementations:
- **REST APIs** - Basic CRUD, Blog API (em breve)
- **Third-Party Mocks** - Stripe, GitHub, Slack APIs (em breve)
- **Microservices** - Service mesh, Event-driven (em breve)

## 🎯 Learning Paths

### I'm entirely new to moclojer
1. Read the [Overview](getting-started/overview.md)
2. Follow the [Installation](getting-started/installation.md) guide
3. Complete all tutorials in the "First Steps" section
4. Explore [Core Concepts](topics/) as needed

### I want to mock a specific type of API
1. Start with [CRUD Operations How-to](how-to/patterns/crud-operations.md) ⭐
2. Check [Examples & Use Cases](examples/) for your use case (em breve)
3. Review [HTTP Methods](topics/endpoints/http-methods.md) ⭐
4. Learn about [Path](topics/parameters/path-parameters.md) ⭐ and [Query Parameters](topics/parameters/query-parameters.md) ⭐
5. Refer to the [Configuration Specification](reference/configuration-spec.md)

### I'm integrating moclojer into my application
1. Read [Using as a Library](framework/using-as-library.md)
2. Check [Testing Integration](framework/testing-integration.md)
3. Review [Development Workflows](framework/development-workflows.md)

### I need help with a specific feature
1. Check the [Troubleshooting Guide](reference/troubleshooting.md) ⭐ first
2. Review the [FAQ](reference/faq.md)
3. Search the [Reference Documentation](reference/)
4. Check [CLI Reference](reference/cli-reference.md) ⭐ for command-line options
5. Look for examples in [How-to Guides](how-to/)

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