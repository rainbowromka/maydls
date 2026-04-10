#!/bin/bash
cd "$(dirname "$0")"
docker compose up -d
echo "PostgreSQL запущен на порту 5432"
echo "База: maydls"
echo "Пользователь: maydls_user"
echo "Пароль: maydls_pass"