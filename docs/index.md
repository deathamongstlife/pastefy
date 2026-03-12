---
# https://vitepress.dev/reference/default-theme-home-page
layout: home

hero:
  name: "pastely Docs"
  tagline: Documentation about pastely
  actions:
    - theme: brand
      text: Features
      link: /features/index
    - theme: brand
      text: API Reference
      link: /api/index
    - theme: alt
      text: Official pastely Instance
      link: https://pastely.app
    - theme: alt
      text: Demo
      link: https://pastely.app
    - theme: alt
      text: Self-Hosting
      link: /self-hosting/index
      
features:
  - icon: 📝
    title: Raw Preview
    details: View the raw content of your pastes directly in the browser.
  - icon: 📋
    title: Copy Button
    details: Quickly copy the content of a paste to your clipboard with a single click.
  - icon: 🍴
    title: Fork
    details: Create a copy of any existing paste to modify or save it under your account.
  - icon: ⚡
    title: API Access
    details: Interact with pastely programmatically via REST API. Clients available for Javascript/Typescript, Java, and Go.
  - icon: 👤
    title: User Accounts & Login
    details: Create an account, organize pastes into folders, view your pastes, and delete them if needed.
  - icon: 💻
    title: Curl Integration
    details: Easily create pastes from the command line using `curl -F f=@file.txt pastely.app`.
  - icon: 🔌
    title: Extensions
    details: Integrations with VS Code and Raycast for seamless workflow.
  - icon: 📂
    title: File Previews
    details: Supports Markdown, Mermaid, SVG, CSV, GeoJSON, Diff, Calendar, Regex, and Asciinema recordings.
  - icon: 🎨
    title: Custom Branding
    details: Add custom logos, names, and footers to your pastely instance.
  - icon: 🔒
    title: Login Control
    details: Require login for reading or creating pastes, enforce admin approval, and control public paste visibility.
  - icon: 🛡️
    title: Encryption
    details: Enable default encryption for all new pastes to enhance privacy.
  - icon: 🐳
    title: Docker & Kubernetes Deployments
    details: Run pastely with Docker, Docker Compose, Kubernetes, or container-less setups.
  - icon: 🛠️
    title: Administration Panel
    details: Access the admin panel to manage users, roles, and system settings.
  - icon: 🎬
    title: Asciinema Integration
    details: Record terminal sessions and upload them to pastely for sharing or archiving.
---

