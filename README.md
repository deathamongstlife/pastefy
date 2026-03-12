<p align="center">
    <h1 align="center">Pastely</h1>
    <p align="center"><em>Advanced code sharing and collaboration platform</em></p>
</p>

<img src="./.github/screenshots/paste.png" width="100%" >

**Pastely** (formerly Pastefy) is an advanced open-source alternative to Gists or Pastebin with powerful features for developers and teams.

Share code, collaborate in real-time, track analytics, and organize your pastes like never before.

## What's New in Pastely 7.0

This is a **massive enhancement** with 150+ new features across 18 categories:

- **Version Control System** - Git-like branching, diffs, and revision history
- **Real-time Collaboration** - Live cursor tracking and collaborative editing
- **Advanced Security** - Password protection, IP filtering, burn-after-read, access logs
- **Social Features** - Follow users, comments, activity feeds, user profiles
- **Analytics & Tracking** - View analytics, geographic tracking, trending algorithm
- **Enhanced Organization** - Collections, nested folders, smart folders
- **Integrations** - Webhooks, GitHub Gist import/export, API enhancements
- **Media Support** - File attachments, image uploads
- **Code Templates** - Reusable templates with categories
- And much more!

Try out the public instance https://pastely.app <br>
Visit the docs for everything around Pastely here: https://docs.pastely.app

## Core Features

### Classic Features
- Raw-Preview & Copy Button
- Fork & Star pastes
- Comprehensive API ([Docs](https://docs.pastely.app/api/))
  - [Javascript/Typescript](https://github.com/interaapps/js-api-clients?tab=readme-ov-file#pastefy)
  - [Java](https://github.com/interaapps/pastely-java-apiclient)
  - [Go](https://github.com/interaapps/pastely-go-api)
- OAuth2 Login (InteraApps, Google, GitHub, Discord, Twitch)
- Folder organization
- CLI upload: `curl -F f=@file.txt pastely.app`
- Extensions: VS-Code, Raycast
- Rich Previews: Markdown, Mermaid, SVG, CSV, GeoJSON, Diff, Calendar, Regex, Asciinema

### NEW: Version Control (7.0)
- Git-like revision history with diffs
- Branch management
- Commit messages
- Rollback to previous versions
- Compare revisions

### NEW: Real-time Collaboration (7.0)
- Live collaborative editing
- Real-time cursor tracking
- Session management with expiration
- Multiple participants support

### NEW: Advanced Security (7.0)
- Password protection with BCrypt
- IP whitelisting/blacklisting
- Burn-after-read (max views)
- Access audit logs
- Detailed security analytics

### NEW: Social Features (7.0)
- Follow/unfollow users
- Comments on pastes (nested support)
- Activity feeds
- User profiles with bio/links
- Social analytics

### NEW: Analytics & Tracking (7.0)
- View tracking with geographic data
- Trending algorithm with time decay
- Timeline visualization
- Unique vs total views
- Average time spent analytics

### NEW: Enhanced Organization (7.0)
- Curated collections
- Nested folders
- Smart folders with filters
- Bulk operations

### NEW: Integrations (7.0)
- Webhook subscriptions
- GitHub Gist import/export
- Enhanced API endpoints
- Event logging

### NEW: Media & Templates (7.0)
- File attachments
- Image uploads with thumbnails
- Code templates library
- Template categories

### Coming Soon
- GraphQL API
- PWA with offline mode
- AI code explanations
- PDF/Image export
- Browser extension

# Overview
- [Screenshots](#Screenshots)
- Deploy
  - [Docker](https://docs.pastely.app/self-hosting/installation/docker.html)
  - [Docker-Compose](https://docs.pastely.app/self-hosting/installation/docker-compose.html)
  - [Container-Less](#Container-Less)
- [Configuration](https://docs.pastely.app/self-hosting/configuration.html)
- [Develop](#Develop)
- [API](https://docs.pastely.app/api/)

# Screenshots
<img src="./.github/screenshots/home.png" width="46%" >
<img src="./.github/screenshots/paste.png" width="46%" >
<img src="./.github/screenshots/new.png" width="46%" >
<img src="./.github/screenshots/fullscreen.png" width="46%" >

## Deploy

See [Self-Hosting](https://docs.pastely.app/self-hosting/index.html) for more options.

### Container-Less
```bash
git clone https://github.com/interaapps/pastely.git
cd pastefy/frontend
npm run install
npm run build
cd ../backend
mvn clean package
cd ..
cp .env.example .env
nano .env
java -jar backend/target/backend.jar
```
Using intelliJ? Look at [Develop](#Develop)

## Configuration
See [Configuration](https://docs.pastely.app/self-hosting/configuration.html) for all options.
### Adding login
You can choose between [INTERAAPPS](https://accounts.interaapps.de/developers) (best integration), [GOOGLE](https://support.google.com/cloud/answer/6158849?hl=en), [GITHUB](https://docs.github.com/en/developers/apps/building-oauth-apps/creating-an-oauth-app), [DISCORD](https://discord.com/developers/docs/topics/oauth2) or [TWITCH](https://dev.twitch.tv/docs/authentication) for the provider (You can use all of them at the same time).
```properties
OAUTH2_${provider}_CLIENT_ID=${client_id}
OAUTH2_${provider}_CLIENT_SECRET=${client_secret}
```
#### Example
```properties
OAUTH2_INTERAAPPS_CLIENT_ID=dan3q9n
OAUTH2_INTERAAPPS_CLIENT_SECRET=ASDFASDF
```


#### Custom OIDC Example
```properties
OAUTH2_CUSTOM_CLIENT_ID=CLIENT_ID
OAUTH2_CUSTOM_CLIENT_SECRET=SECRET
OAUTH2_CUSTOM_AUTH_ENDPOINT=https://accounts.interaapps.de/auth/oauth2
OAUTH2_CUSTOM_TOKEN_ENDPOINT=https://accounts.interaapps.de/api/v2/authorization/oauth2/access_token
OAUTH2_CUSTOM_USERINFO_ENDPOINT=https://accounts.interaapps.de/api/v2/oidc/userinfo
```

## Develop

#### Build frontend into the backend
```bash
# You might want to build the frontend
cd frontend
npm build prod
```

#### Frontend
Run the backend (On port 1337) and then go to the frontend and run
```bash
cd frontend
npm run serve
```

We are using IntelliJ Idea and Visual Studio code.

### API
You can find the docs of the Pastefy-Rest-APi here: [Docs](https://docs.pastely.app/api/)


## Administration
If you want to give yourself the admin role, you have to log into your MySQL server and set `type` on your account to `ADMIN` in the `pastely_users` table.

You'll find the admin panel under `https://YOUR_URL/admin`


## Extra Features

Read more here [Docs](https://docs.pastely.app/features/index.html)

### Upload via Curl
```bash
curl -F f=@file.txt pastely.app
```
### Asciinema support

configure: `nano ~/.config/asciinema/config`
```
[api]
url = https://pastely.app
```

Using asciinema
```bash
asciinema rec test.cast
# ...
asciinema upload test.cast

# Authenticate via Pastefy
# Pastefy will request you to set the install-id via `echo YOUR_PASTEFY_API_KEY > ~/.config/asciinema/install-id`
asciinema auth

asciinema upload test.cast
```