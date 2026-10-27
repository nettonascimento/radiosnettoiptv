# IPTV RadiosNetto

Site estático que publica a lista IPTV RadiosNetto num link fixo (`/lista.m3u`, também em `/lista.m3u8`) e traz uma página para procurar os canais, filtrar por categoria e assisti-los no navegador.

## Tecnologias
- HTML/CSS/JS puro, sem etapa de build
- hls.js (via CDN) para tocar os streams no navegador
- Hospedagem na Netlify (`netlify.toml` define a pasta `public` e os cabeçalhos da lista)

## Como atualizar a lista
Troque o arquivo `public/lista.m3u` (e a cópia `public/lista.m3u8`) e faça o deploy. O link continua o mesmo, então os aplicativos recebem os canais novos sozinhos.

## Rodar localmente
```
netlify dev
```
ou abra a pasta `public` com qualquer servidor estático.
