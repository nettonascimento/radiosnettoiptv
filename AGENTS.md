# AGENTS.md

- Site estático, sem build. Pasta publicada: `public/`.
- `public/lista.m3u` é a lista IPTV (a fonte da verdade); `public/lista.m3u8` é uma cópia idêntica. Atualize sempre as duas juntas.
- `public/index.html` lê `/lista.m3u` no navegador, extrai `tvg-logo`/`group-title`/nome e monta a grade de canais e o player (hls.js via CDN).
- `netlify.toml` define o Content-Type `audio/x-mpegurl`, libera CORS e desativa o cache dos arquivos da lista para que os apps recebam as atualizações na hora.
- Cada canal precisa de uma linha `#EXTINF` (com o `#`) seguida da URL. Ao receber uma lista nova, corrija as linhas sem `#`.
- Não inclua canais pagos por assinatura (HBO, Telecine, Premiere, ESPN, SporTV, etc.) na lista pública.
