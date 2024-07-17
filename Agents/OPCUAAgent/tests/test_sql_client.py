from datetime import datetime, timezone
import datetime
import pytest
import tempfile
import os
import pathlib
from unittest.mock import patch
import time
from psycopg2 import sql

from OPCUAAgent import sql_client

class Test_sql_client:
    def setUp(self):
        self._temp_dir = tempfile.TemporaryDirectory()
        self.temp_path = pathlib.Path(self._temp_dir.name)
        self._create_temporary_file_with_data(self.temp_path / 'test_conf.properties',
                                              'dbname=postgres\n' +
                                              'user=postgres\n' +
                                              'password=postgres\n' +
                                              'host=postgres_agent_test\n' +
                                              'port=5432\n')
        
    def setUp_fail_non_existent_db(self):
        self._temp_dir = tempfile.TemporaryDirectory()
        self.temp_path = pathlib.Path(self._temp_dir.name)
        self._create_temporary_file_with_data(self.temp_path / 'test_conf.properties',
                                              'dbname=non_existent_db\n' +
                                              'user=postgres\n' +
                                              'password=postgres\n' +
                                              'host=postgres_agent_test\n' +
                                              'port=5432\n')
        
    def setUp_new_db(self):
        self._temp_dir = tempfile.TemporaryDirectory()
        self.temp_path = pathlib.Path(self._temp_dir.name)
        self._create_temporary_file_with_data(self.temp_path / 'test_conf.properties',
                                              'dbname=test\n' +
                                              'user=postgres\n' +
                                              'password=postgres\n' +
                                              'host=postgres_agent_test\n' +
                                              'port=5432\n')
    
    def _create_temporary_file_with_data(self, file_path, content):
        with open(file_path, 'w') as ifile:
            ifile.write(content)

    def tearDown(self):
        self._temp_dir.cleanup()
        
    def test_create_database_if_not_exist_fail(self):
        self.setUp_new_db()
        #sleep test to wait for postgresql container to spin up
        time.sleep(3)
        with pytest.raises(Exception) as excinfo:
            sql_client.create_database_if_not_exist()
        assert "Error while connecting to PostgreSQL, do check the environment variable and properties file..." in str(excinfo.value)
        self.tearDown()
            
    def test_create_database_if_not_exist_success(self):
        self.setUp_new_db()
        #sleep test to wait for postgresql container to spin up
        time.sleep(3)
        with patch.dict(os.environ, {"POSTGRES_CONF":str(self.temp_path / 'test_conf.properties')}, clear=True):
            sql_client.create_database_if_not_exist()
            connection = sql_client.connect_to_database()
        assert connection != None
        assert "test" == connection.info.dbname
          
    def test_connect_to_database_fail(self):
        self.setUp_fail_non_existent_db()
        #sleep test to wait for postgresql container to spin up
        time.sleep(3)
        with patch.dict(os.environ, {"POSTGRES_CONF":str(self.temp_path / 'test_conf.properties')}, clear=True):
            with pytest.raises(Exception) as excinfo:
                sql_client.connect_to_database()
            assert "FATAL:  database \"non_existent_db\" does not exist" in str(excinfo.value)
        self.tearDown()
        
    def test_connect_to_database_success(self):
        self.setUp()
        #sleep test to wait for postgresql container to spin up
        time.sleep(3)
        with patch.dict(os.environ, {"POSTGRES_CONF":str(self.temp_path / 'test_conf.properties')}, clear=True):
            connection = sql_client.connect_to_database()
        assert connection != None
        assert "postgres" == connection.info.dbname
        self.tearDown()
        
    def test_create_if_not_exist_and_insert(self):
        self.setUp_new_db()
        time.sleep(3)
        with patch.dict(os.environ, {"POSTGRES_CONF":str(self.temp_path / 'test_conf.properties')}, clear=True):
            sql_client.create_database_if_not_exist()
            connection = sql_client.connect_to_database()
            timestamp = datetime.datetime.now(timezone.utc).isoformat(timespec='seconds')
            values_dict = {"testing":{"tag_01":{"timestamp":timestamp, "value":123, "data_type":"Float"}, "tag_02":{"timestamp":timestamp, "value":12, "data_type":"Float"}}}
            sql_client.create_if_not_exist_and_insert(connection, values_dict)
        
        cursor = connection.cursor()
        cursor.execute(
                    sql.SQL("SELECT * FROM {}.{}").format(
                    sql.Identifier("opcua_pips"), sql.Identifier("testing")))
        existing_record = cursor.fetchall()
        assert 1 == len(existing_record)
        self.tearDown()
    
