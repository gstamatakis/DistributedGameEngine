#!/bin/bash

set -o errexit
set -o nounset
set -o pipefail

# Check if the script is currently running as root
am_i_root() {
  if [[ "$(id -u)" == "0" ]]; then
    true
  else
    false
  fi
}

ip=$(hostname -i)
args=("--daemonize" "no" "--slave-announce-ip" "${ip}" "$@")

echo "INFO Starting Redis" >&2

if am_i_root; then
  exec gosu redis "redis-server" "${args[@]}"
else
  exec "redis-server" "${args[@]}"
fi
