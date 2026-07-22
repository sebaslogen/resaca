# Security Policy

## Reporting a Vulnerability

If you believe you've found a security vulnerability in Resaca, please report it privately using
[GitHub's Private Vulnerability Reporting](https://github.com/sebaslogen/resaca/security/advisories/new)
feature (found under the **Security** tab of this repository). This keeps the report private while a fix
is developed and allows a CVE to be issued if warranted.

Please do not open a public GitHub issue for security vulnerabilities.

When reporting, include as much of the following as you can:

- A description of the vulnerability and its potential impact
- Steps to reproduce, or a minimal sample project
- The affected version(s) of the library
- Any relevant logs or stack traces

You should receive an acknowledgement within 5 business days. This project is maintained on a best-effort,
volunteer basis, so please be patient — you'll be kept updated as the report is triaged and, if confirmed,
fixed.

## Scope

Resaca is a Kotlin Multiplatform/Compose library for scoping objects and ViewModels to Composables. It does
not handle secrets, cryptography, networking, or persistent storage directly, so the realistic scope for
security reports is:

- The library's Kotlin/Compose source code (`resaca`, `resacahilt`, `resacakoin`, `resacametro` modules)
- The build and publishing pipeline (GitHub Actions workflows, Maven Central publication)
- Vulnerable dependencies pulled in transitively

Non-security bugs, crashes, or feature requests should go through the regular
[issue tracker](https://github.com/sebaslogen/resaca/issues) instead.

## Supported Versions

Resaca does not maintain long-term support branches. Security fixes are released against the latest
version published on [Maven Central](https://central.sonatype.com/artifact/io.github.sebaslogen/resaca).
If you're reporting a vulnerability, please confirm it's still present on the latest release first, and
always upgrade to the latest version to receive fixes.

## Disclosure Policy

This project follows coordinated disclosure: once a report is confirmed, a fix is prepared and released
before any public details are shared. Reporters are credited (unless they prefer to remain anonymous) once
the fix is out.
