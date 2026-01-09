# katorabian-ai-core

Core Platform repository for the my AI system.

This repository represents the heart of the platform and contains all foundational components required to run, orchestrate, and extend the AI infrastructure.

---

## Overview

`katorabian-ai-core` serves as the **central backbone** of the entire system.  
It defines the core runtime, infrastructure, and communication layer for all AI-related services.

All other repositories depend on this one either directly or indirectly.

---

## Repository Structure

- **docker-compose/**  
  Environment orchestration for local development and production deployments.

- **infrastructure/**  
  Core infrastructure configuration, networking, and base services.

- **ai-gateway/** *(Python)*  
  Primary entry point for external and internal requests.  
  Handles routing, orchestration, and coordination between system components.

- **llm/**  
  Language model integrations.

- **memory/**  
  Persistent and ephemeral memory subsystems.

- **tools/**  
  Tooling layer used by LLMs and higher-level agents.

---

## Responsibilities

This repository is responsible for:

- Defining the system runtime and execution model
- Hosting core AI services and shared logic
- Orchestrating interactions between LLMs, memory, and tools
- Providing a stable foundation for higher-level applications and feature repositories

---

## Usage

This repository is intended to be:

- The first component started in any environment
- A required dependency for all downstream services
- The single source of truth for core AI infrastructure

---

## Notes

- Changes in this repository may affect the entire platform
- Versioning and stability should be handled with care
- Downstream repositories should avoid duplicating logic defined here

---

## Status

Actively developed.  
Breaking changes are possible while the platform architecture is evolving.
