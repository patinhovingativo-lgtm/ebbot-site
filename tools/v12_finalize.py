from pathlib import Path

root = Path(__file__).resolve().parents[1]
catalog = root / "cefet-trainer/app/src/main/java/com/cefettrainer/app/StudyCatalog.java"
extra = root / "cefet-trainer/app/src/main/java/com/cefettrainer/app/StudyCatalogExtra.java"
text = catalog.read_text(encoding="utf-8")

# Corrige uma quebra de linha acidental dentro de literal Java no bloco de tese.
text = text.replace(
    "(argumento apoiado por evidências).'\n\",",
    "(argumento apoiado por evidências).'\","
)

# Integra o catálogo complementar ao catálogo principal usado por tutor e simulado.
needle = "        return m;\n    }\n\n    public static String subjectIntro"
replacement = "        m.putAll(StudyCatalogExtra.create());\n        return m;\n    }\n\n    public static String subjectIntro"
if "m.putAll(StudyCatalogExtra.create());" not in text:
    if needle not in text:
        raise SystemExit("não foi possível localizar o final de StudyCatalog.create()")
    text = text.replace(needle, replacement, 1)

catalog.write_text(text, encoding="utf-8")

if not extra.exists():
    raise SystemExit("StudyCatalogExtra.java ausente")
extra_text = extra.read_text(encoding="utf-8")

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
        raise SystemExit(f"remaster marker ausente: {marker}")

catalog_text = catalog.read_text(encoding="utf-8")
for marker in ["Substantivo", "Singular e plural", "Equação do 1º grau", "Equação do 2º grau", "Função afim", "Inferência", "StudyCatalogExtra.create"]:
    if marker not in catalog_text:
        raise SystemExit(f"conteúdo essencial ausente: {marker}")

for marker in ["Crase", "Radiciação", "Lei de Ohm", "Seleção natural", "Revolução Francesa", "Geopolítica"]:
    if marker not in extra_text:
        raise SystemExit(f"conteúdo complementar ausente: {marker}")

print("final remaster source finalization OK")
