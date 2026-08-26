from pathlib import Path

root = Path(__file__).resolve().parents[1]
catalog = root / "cefet-trainer/app/src/main/java/com/cefettrainer/app/StudyCatalog.java"
text = catalog.read_text(encoding="utf-8")

# Corrige uma quebra de linha acidental dentro de literal Java no bloco de tese.
text = text.replace(
    "(argumento apoiado por evidências).'\n\",",
    "(argumento apoiado por evidências).'\","
)

catalog.write_text(text, encoding="utf-8")

activity = root / "cefet-trainer/app/src/main/java/com/cefettrainer/app/RemasteredActivity.java"
a = activity.read_text(encoding="utf-8")
required = [
    "Reconstruir minha base",
    "AGORA TESTE NO MEIO DA EXPLICAÇÃO",
    "Cobertura",
    "Domínio",
    "Retenção",
    "Precisão",
    "Lacuna provável detectada",
    "setScrollbarFadingEnabled(false)",
]
for marker in required:
    if marker not in a:
        raise SystemExit(f"v12 marker ausente: {marker}")

catalog_text = catalog.read_text(encoding="utf-8")
for marker in ["Substantivo", "Singular e plural", "Equação do 1º grau", "Equação do 2º grau", "Função afim", "Inferência"]:
    if marker not in catalog_text:
        raise SystemExit(f"conteúdo essencial ausente: {marker}")

print("v0.12 source finalization OK")
