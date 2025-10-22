#!/usr/bin/env bash
# Robin service control script for Ubuntu/Debian systems.
# Provides: start | stop | restart | status | tail | help
#
# Java command baseline (can be overridden via environment variables):
#   java -server -Xms256m -Xmx1024m -Dlog4j.configurationFile=/usr/local/robin/cfg/log4j2.xml \
#        -cp "/usr/local/robin/robin.jar:/usr/local/robin/lib/*" com.mimecast.robin.Main --server /usr/local/robin/cfg/
#
# Environment overrides:
#   ROBIN_JAVA_OPTS   Additional JVM opts (e.g. -Xms512m -Xmx1536m)
#   ROBIN_HOME        Base install directory (default /usr/local/robin)
#   ROBIN_USER        Run as this user if script invoked with sudo/root (default vmail)
#   ROBIN_PID_FILE    Custom PID file path
#   ROBIN_LOG_FILE    Redirect stdout/stderr to this file (default $ROBIN_HOME/robin.console.log)
#
# Exit codes:
#   0 success
#   1 general error
#   2 already running / already stopped conditions
#   3 dependency (java) missing
#   4 insufficient permissions
#
set -euo pipefail
IFS=$'\n\t'

APP_NAME="robin"
ROBIN_HOME="${ROBIN_HOME:-/usr/local/robin}"
PID_FILE="${ROBIN_PID_FILE:-${ROBIN_HOME}/${APP_NAME}.pid}"
LOG_FILE="${ROBIN_LOG_FILE:-${ROBIN_HOME}/${APP_NAME}.console.log}"
RUN_USER="${ROBIN_USER:-vmail}"
JAVA_BIN="${JAVA_BIN:-java}"
BASE_JAVA_OPTS="-server -Xms256m -Xmx1024m -Dlog4j.configurationFile=${ROBIN_HOME}/cfg/log4j2.xml"
JAVA_OPTS="${ROBIN_JAVA_OPTS:-}" # user supplied extra opts
CLASSPATH="${ROBIN_HOME}/robin.jar:${ROBIN_HOME}/lib/*"
MAIN_CLASS="com.mimecast.robin.Main"
MAIN_ARGS="--server ${ROBIN_HOME}/cfg/"

# Colors (disable with NO_COLOR=1)
if [[ "${NO_COLOR:-}" == "1" ]]; then
  C_RESET=""; C_RED=""; C_GREEN=""; C_YELLOW=""; C_BLUE=""; C_DIM=""
else
  C_RESET='\033[0m'; C_RED='\033[0;31m'; C_GREEN='\033[0;32m'; C_YELLOW='\033[0;33m'; C_BLUE='\033[0;34m'; C_DIM='\033[2m'
fi

msg() { echo -e "${1}"; }
info() { msg "${C_BLUE}[INFO]${C_RESET} $1"; }
success() { msg "${C_GREEN}[OK]${C_RESET} $1"; }
warn() { msg "${C_YELLOW}[WARN]${C_RESET} $1"; }
error() { msg "${C_RED}[ERROR]${C_RESET} $1" >&2; }

die() { error "$1"; exit "${2:-1}"; }

require_java() {
  if ! command -v "${JAVA_BIN}" >/dev/null 2>&1; then
    die "java runtime not found in PATH (JAVA_BIN='${JAVA_BIN}')." 3
  fi
}

is_running() {
  if [[ -f "${PID_FILE}" ]]; then
    local pid
    pid="$(cat "${PID_FILE}")" || return 1
    if [[ -n "${pid}" && -d "/proc/${pid}" ]]; then
      return 0
    fi
  fi
  return 1
}

start() {
  if is_running; then
    warn "${APP_NAME} already running (pid $(cat "${PID_FILE}"))."; exit 2
  fi
  require_java
  mkdir -p "${ROBIN_HOME}" "${ROBIN_HOME}/lib" "${ROBIN_HOME}/cfg"
  touch "${LOG_FILE}" || die "Cannot write to log file ${LOG_FILE}" 4

  local cmd=("${JAVA_BIN}" ${BASE_JAVA_OPTS} ${JAVA_OPTS} -cp "${CLASSPATH}" "${MAIN_CLASS}" ${MAIN_ARGS})

  info "Starting ${APP_NAME}...";
  if [[ $(id -u) -eq 0 && "${RUN_USER}" != "root" ]]; then
    # Use runuser or su to drop privileges
    if command -v runuser >/dev/null 2>&1; then
      nohup runuser -u "${RUN_USER}" -- "${cmd[@]}" >>"${LOG_FILE}" 2>&1 &
    else
      nohup su - "${RUN_USER}" -c "${cmd[*]}" >>"${LOG_FILE}" 2>&1 &
    fi
  else
    nohup "${cmd[@]}" >>"${LOG_FILE}" 2>&1 &
  fi
  local pid=$!
  echo "${pid}" > "${PID_FILE}"
  sleep 1
  if is_running; then
    success "${APP_NAME} started with PID ${pid}. Log: ${LOG_FILE}"; exit 0
  else
    die "Failed to start ${APP_NAME}, see ${LOG_FILE}." 1
  fi
}

stop() {
  if ! is_running; then
    warn "${APP_NAME} not running."; exit 2
  fi
  local pid
  pid="$(cat "${PID_FILE}")"
  info "Stopping ${APP_NAME} (pid ${pid})..."
  kill "${pid}" || warn "Primary TERM failed (pid ${pid})."
  local waited=0
  local timeout=25
  while is_running && [[ ${waited} -lt ${timeout} ]]; do
    sleep 1; waited=$((waited+1))
  done
  if is_running; then
    warn "Process still up after ${timeout}s, sending KILL."; kill -9 "${pid}" || true
  fi
  rm -f "${PID_FILE}" || true
  success "${APP_NAME} stopped."; exit 0
}

status() {
  if is_running; then
    local pid; pid="$(cat "${PID_FILE}")"
    success "${APP_NAME} is running (pid ${pid})."; exit 0
  else
    warn "${APP_NAME} is not running."; exit 1
  fi
}

restart() {
  stop || true
  start
}

tail_log() {
  if [[ ! -f "${LOG_FILE}" ]]; then
    die "Log file ${LOG_FILE} does not exist." 1
  fi
  info "Tailing ${LOG_FILE} (Ctrl+C to exit)";
  tail -F "${LOG_FILE}"
}

usage() {
  cat <<EOF
${APP_NAME} service control script

Usage: $0 {start|stop|restart|status|tail|help}

Environment overrides:
  ROBIN_HOME       (${ROBIN_HOME})
  ROBIN_USER       (${RUN_USER})
  ROBIN_JAVA_OPTS  (extra JVM opts)
  JAVA_BIN         (${JAVA_BIN})
  ROBIN_PID_FILE   (${PID_FILE})
  ROBIN_LOG_FILE   (${LOG_FILE})
  NO_COLOR=1       Disable colored output

Examples:
  sudo $0 start
  sudo ROBIN_JAVA_OPTS='-Xms512m -Xmx1536m' $0 restart
  $0 status
  $0 tail
EOF
}

main() {
  local action="${1:-help}";
  case "${action}" in
    start) start ;;
    stop) stop ;;
    restart) restart ;;
    status) status ;;
    tail) tail_log ;;
    help|--help|-h) usage ;;
    *) error "Unknown action: ${action}"; usage; exit 1 ;;
  esac
}

main "$@"

