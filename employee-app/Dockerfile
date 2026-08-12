FROM python:3.11-slim AS build

WORKDIR /app

RUN apt-get update && apt-get install -y --no-install-recommends \
    build-essential \
    && rm -rf /var/lib/apt/lists/*

RUN python -m venv /opt/venv
ENV PATH="/opt/venv/bin:$PATH"

COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

FROM python:3.11-slim

WORKDIR /app

COPY --from=build /opt/venv /opt/venv
ENV PATH="/opt/venv/bin:$PATH"

RUN useradd --create-home appuser && chown appuser:appuser /app
USER appuser

COPY --chown=appuser:appuser . .

EXPOSE 5001

# no curl in this slim image; use python itself instead
HEALTHCHECK --interval=10s --timeout=5s --start-period=15s --retries=3 \
  CMD python3 -c "import urllib.request; urllib.request.urlopen('http://localhost:5001/login')" || exit 1

CMD ["gunicorn", "--bind", "0.0.0.0:5001", "app:app"]
