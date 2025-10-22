#!/bin/bash

set -euo pipefail

if [ "$(id -u)" -ne 0 ]; then
  echo "This setup script must be run as root (or with sudo)." >&2
  exit 1
fi

echo "[1/6] Preparing directories..."
mkdir -p /usr/local/robin
mkdir -p /usr/local/robin/cfg
mkdir -p /usr/local/robin/lib
mkdir -p /usr/local/robin/store/queue
chmod -R 755 /usr/local/robin
chown -R vmail:vmail /usr/local/robin

echo "[2/6] Building project (skipping tests)..."
mvn clean package -Dmaven.test.skip=true

# Copy dependency JARs produced by maven-dependency-plugin into lib directory.
# Original line 'cp target/classes/lib /usr/local/robin/lib' would fail (needs -r) and create a nested 'lib' directory.
if [ -d target/classes/lib ]; then
  echo "[3/6] Copying dependency libs..."
  cp -r target/classes/lib/* /usr/local/robin/lib/ || true
else
  echo "[WARN] target/classes/lib not found; dependency jars may be missing." >&2
fi

# Copy main application jar (finalName=robin => target/robin.jar)
if [ -f target/robin.jar ]; then
  echo "[4/6] Deploying application jar..."
  cp target/robin.jar /usr/local/robin/robin.jar
else
  echo "[ERROR] target/robin.jar not found. Build may have failed." >&2
  exit 1
fi

# Copy keystore
cp src/test/resources/keystore.jks /usr/local/robin/keystore.jks

# Copy configuration files (avoid creating /usr/local/robin/cfg/cfg)
if [ -d cfg-prod ]; then
  echo "[5/6] Copying production configuration files..."
  cp -r cfg-prod/* /usr/local/robin/cfg/
else
  echo "[WARN] cfg-prod directory not found; proceeding without production configuration." >&2
fi

# Install control script
cp robin.sh /usr/local/robin/robin.sh
chmod 755 /usr/local/robin/robin.sh
chown vmail:vmail /usr/local/robin/robin.sh

# Install systemd service unit (must be owned by root and NOT executable)
if command -v systemctl >/dev/null 2>&1; then
  cp robin.service /etc/systemd/system/robin.service
  chmod 644 /etc/systemd/system/robin.service
  chown root:root /etc/systemd/system/robin.service

  echo "[6/6] Enabling and starting systemd service..."
  systemctl daemon-reload
  systemctl enable robin.service
  systemctl restart robin.service
  systemctl --no-pager status robin.service || true
else
  echo "[INFO] systemctl not available; skipping service installation. Use /usr/local/robin/robin.sh start manually." >&2
fi

echo "Setup complete."
