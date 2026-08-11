"""
Database connection factory.
Provides connections for either SQLite (local testing)
or PostgreSQL (Docker/AWS deployment).
"""

import os
import sqlite3
import psycopg2

from logger import get_logger

logger = get_logger(__name__)

DB_TYPE = os.getenv("DB_TYPE", "postgres")


def get_connection():

    if DB_TYPE == "postgres":

        DB_HOST = os.getenv("DB_HOST", "localhost")
        DB_PORT = os.getenv("DB_PORT", "5432")
        DB_NAME = os.getenv("DB_NAME", "expense_manager")
        DB_USER = os.getenv("DB_USER", "postgres")
        DB_PASSWORD = os.getenv("DB_PASSWORD", "newPostgresqlUser26")
        # RDS requires SSL, but the local docker-compose postgres isn't built with it.
        # Defaults to "require" so AWS keeps working; compose sets DB_SSLMODE=disable.
        DB_SSLMODE = os.getenv("DB_SSLMODE", "require")

        conn = psycopg2.connect(
            host=DB_HOST,
            port=DB_PORT,
            dbname=DB_NAME,
            user=DB_USER,
            password=DB_PASSWORD,
            sslmode=DB_SSLMODE
        )

        logger.debug(f"Connected to Postgres: {conn.get_dsn_parameters()}")
        return conn

    else:
        SQLITE_PATH = os.getenv(
            "SQLITE_PATH",
            "database/expense_manager.db"
        )

        conn = sqlite3.connect(SQLITE_PATH)

        logger.debug(f"Connected to SQLite: {SQLITE_PATH}")
        return conn
