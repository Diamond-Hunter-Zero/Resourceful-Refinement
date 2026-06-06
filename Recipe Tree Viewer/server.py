#!/usr/bin/env python3
"""Local web server for the Recipe Tree Viewer UI."""

from __future__ import annotations

import argparse
import json
import urllib.parse
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer
from pathlib import Path

from recipe_tree import RecipeIndex, build_trees, parse_recipe_selections

ROOT = Path(__file__).resolve().parent
DEFAULT_RECIPE_ROOT = (
    ROOT.parent
    / "src"
    / "main"
    / "resources"
    / "data"
    / "resourceful_refinement"
    / "recipe"
)


class Handler(BaseHTTPRequestHandler):
    index: RecipeIndex | None = None
    recipe_root: Path = DEFAULT_RECIPE_ROOT

    def log_message(self, format: str, *args) -> None:  # noqa: A003
        return

    def _send_json(self, payload: dict, status: int = 200) -> None:
        body = json.dumps(payload).encode("utf-8")
        self.send_response(status)
        self.send_header("Content-Type", "application/json")
        self.send_header("Content-Length", str(len(body)))
        self.send_header("Access-Control-Allow-Origin", "*")
        self.end_headers()
        self.wfile.write(body)

    def _send_file(self, path: Path, content_type: str) -> None:
        if not path.is_file():
            self.send_error(404)
            return
        data = path.read_bytes()
        self.send_response(200)
        self.send_header("Content-Type", content_type)
        self.send_header("Content-Length", str(len(data)))
        self.end_headers()
        self.wfile.write(data)

    def do_POST(self) -> None:  # noqa: N802
        parsed = urllib.parse.urlparse(self.path)
        if parsed.path == "/api/reload":
            Handler.index = RecipeIndex.load(Handler.recipe_root)
            self._send_json(
                {
                    "reloaded": True,
                    "stats": Handler.index.stats(),
                    "recipe_root": str(Handler.recipe_root),
                }
            )
            return
        self.send_error(404)

    def do_GET(self) -> None:  # noqa: N802
        parsed = urllib.parse.urlparse(self.path)
        if parsed.path in ("/", "/index.html"):
            self._send_file(ROOT / "index.html", "text/html; charset=utf-8")
            return

        if parsed.path == "/api/stats":
            if Handler.index is None:
                Handler.index = RecipeIndex.load(Handler.recipe_root)
            self._send_json({"stats": Handler.index.stats(), "recipe_root": str(Handler.recipe_root)})
            return

        if parsed.path == "/api/tree":
            params = urllib.parse.parse_qs(parsed.query)
            target = (params.get("target") or [""])[0]
            if not target:
                self._send_json({"error": "Missing target query parameter"}, status=400)
                return
            amount = float((params.get("amount") or ["1"])[0])
            kind = (params.get("kind") or [None])[0]
            provided: list[str] = []
            for entry in params.get("provided", []):
                for part in entry.split(","):
                    part = part.strip()
                    if part:
                        provided.append(part)
            recipe_selections = parse_recipe_selections(params.get("sel", []))
            simplify = (params.get("simplify") or ["0"])[0].lower() in ("1", "true", "yes")

            if Handler.index is None:
                Handler.index = RecipeIndex.load(Handler.recipe_root)

            result = build_trees(
                Handler.index,
                target,
                amount,
                kind=kind,
                provided_inputs=provided,
                recipe_selections=recipe_selections,
                simplify=simplify,
            )
            self._send_json(result)
            return

        self.send_error(404)


def main() -> None:
    parser = argparse.ArgumentParser(description="Serve the Recipe Tree Viewer")
    parser.add_argument("--host", default="127.0.0.1")
    parser.add_argument("--port", type=int, default=8765)
    parser.add_argument("--recipe-root", type=Path, default=DEFAULT_RECIPE_ROOT)
    args = parser.parse_args()

    Handler.recipe_root = args.recipe_root.resolve()
    Handler.index = None

    if not Handler.recipe_root.is_dir():
        raise SystemExit(f"Recipe root not found: {Handler.recipe_root}")

    server = ThreadingHTTPServer((args.host, args.port), Handler)
    print(f"Recipe Tree Viewer: http://{args.host}:{args.port}")
    print(f"Recipe root: {Handler.recipe_root}")
    server.serve_forever()


if __name__ == "__main__":
    main()
