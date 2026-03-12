# Develop Plugins

## File structure
```
- pastely-app.jar
- .env
...
- plugins/
    - my-plugin/
        - plugin.json
        - frontend/
            - plugin.js
        - backend:
            - backend.jar
```

## plugin.json
```json
{
  "name": "my-plugin",
  "frontend": {
    "folder": "frontend",
    "entrypoint": "plugin.js"
  },
  "backend": {
    "file": "backend/backend.jar",
    "main": "com.example.MyPlugin"
  }
}
```

## Plain Frontend
```js
const pastely = window.pastely;
const { h } = pastely.vueFunctions

pastely.createPlugin('my-plugin', {
    async init() {
        pastely.componentInjections.registerInjection('paste-below-title', ({ value }) => {
            console.log(value.title)
            return h('div', { class: 'bar', innerHTML: 'hello' })
        })
    }
})
```