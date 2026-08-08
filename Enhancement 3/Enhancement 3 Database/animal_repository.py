import os
# dotenv loads the values stored inside the .env file
from dotenv import load_dotenv
from pymongo import MongoClient, ASCENDING
from pymongo.errors import PyMongoError
# Load the environment variables before connecting to MongoDB
load_dotenv()
class AnimalRepository:
    def __init__(self):
        mongo_uri = os.getenv("MONGO_URI", "mongodb://localhost:27017")

        # Read the database name from the .env file.
        database_name = os.getenv("MONGO_DATABASE", "aac")

        # Read the collection name from the .env file.
        collection_name = os.getenv("MONGO_COLLECTION", "animals")

        try:
            # Create the MongoDB connection with timeout
            self.client = MongoClient(mongo_uri, serverSelectionTimeoutMS=5000)

            #confirm MongoDB is available.
            self.client.admin.command("ping")

            # Select the animal shelter database.
            self.database = self.client[database_name]

            # Select the animal collection inside the database.
            self.collection = self.database[collection_name]

            print("Connected to MongoDB")

        except PyMongoError as error:
            # Use a simple error message
            raise RuntimeError("The application could not connect to MongoDB.") from error

    def find(self, query=None, projection=None, sort_field=None, sort_direction=ASCENDING, page=0, page_size=None):
        #returns records

        try:
            # This returns the matching animal records.
            cursor = self.collection.find(query or {},projection)

            # Sort the results when a sort field is provided.
            if sort_field:
                cursor = cursor.sort(sort_field,sort_direction)

            # Apply database pagination when a page size is provided.
            if page_size:
                cursor = cursor.skip(page * page_size).limit(page_size)

            # Convert the MongoDB cursor into a normal Python list.
            return list(cursor)

        except PyMongoError as error:
            # error for not finding records
            raise RuntimeError("Animal records could not be retrieved.") from error

    def aggregate(self, pipeline):
        #aggregation pipeline.

        if not isinstance(pipeline, list):
            raise ValueError("The aggregation pipeline must be a list.")

        try:
            # MongoDB handles the filtering, grouping, and sorting.
            return list(self.collection.aggregate(pipeline))

        except PyMongoError as error:
            raise RuntimeError("The database summary could not be created.") from error

    def create(self, data):
        #Insert one animal record.
        if not isinstance(data, dict) or not data:
            raise ValueError("Animal data must be filled out.")

        try:
            # Insert the new animal record into MongoDB.
            result = self.collection.insert_one(data)

            # Return True when MongoDB confirms the insert.
            return result.acknowledged

        except PyMongoError as error:
            # error if cannot be created
            raise RuntimeError("The animal record could not be created.") from error

    def update(self, query, new_values):
        # An update requires both a search query and new values.
        if not query or not new_values:
            raise ValueError("An update requires query and new values.")

        try:
            # Update every animal record that matches the query. The $set command changes only the chosen fields.
            result = self.collection.update_many(query, {"$set": new_values})

            # Return the number of records that were changed.
            return result.modified_count

        except PyMongoError as error:
            #error if the update fails.
            raise RuntimeError("The animal records could not be updated.") from error

    def delete(self, query):
    #Delete records
        if not query:
            raise ValueError("A delete operation requires a query.")

        try:
            # Delete every animal record that matches the query.
            result = self.collection.delete_many(query)

            # Return the number of records that were deleted.
            return result.deleted_count

        except PyMongoError as error:
            # error if the delete operation fails.
            raise RuntimeError("The animal records could not be deleted.") from error
    def create_indexes(self):
        # Create indexes for fields commonly used by the dashboard.

        try:
            index_names = []
            # Get the indexes that already exist in MongoDB
            existing_indexes = self.collection.index_information()       
            indexed_fields = []

            for index_information in existing_indexes.values():
            # Check each field used by the index
                for field_name, direction in index_information.get("key", []):
                    if field_name not in indexed_fields:
                        indexed_fields.append(field_name)

        # Create the animal type index if it does not already exist
            if "animal_type" not in indexed_fields:
                index_names.append(self.collection.create_index([("animal_type", ASCENDING)], name="animal_type_index"))

        # Create the breed index if it does not already exist
            if "breed" not in indexed_fields:
                index_names.append(self.collection.create_index([("breed", ASCENDING)], name="breed_index"))

        # Create the outcome type index if it does not already exist
            if "outcome_type" not in indexed_fields:
                index_names.append(self.collection.create_index([("outcome_type", ASCENDING)], name="outcome_type_index"))

        # Create the sex upon outcome index if it does not already exist
            if "sex_upon_outcome" not in indexed_fields:
                index_names.append(self.collection.create_index([("sex_upon_outcome", ASCENDING)], name="sex_upon_outcome_index"))
            return index_names

        except PyMongoError as error:
            print(error)
            raise RuntimeError("The database indexes could not be created.") from error

    def apply_schema_validation(self):
        # Add MongoDB validation rules without changing old records.
        validation_rules = {
            "$jsonSchema": {
                "bsonType": "object",
                "required": [
                    "animal_id",
                    "animal_type",
                    "breed",
                    "sex_upon_outcome"
                ],
                "properties": {
                    "animal_id": {
                        "bsonType": "string",
                        "description": "Animal ID must be text."
                    },
                    "animal_type": {
                        "bsonType": "string",
                        "description": "Animal type must be text."
                    },
                    "breed": {
                        "bsonType": "string",
                        "description": "Breed must be text."
                    },
                    "sex_upon_outcome": {
                        "bsonType": "string",
                        "description": "Sex upon outcome must be text."
                    },
                    "age_upon_outcome_in_weeks": {
                        "bsonType": [
                            "double",
                            "int",
                            "long",
                            "decimal"
                        ],
                        "description": "Age in weeks must be a number."
                    },
                    "location_lat": {
                        "bsonType": [
                            "double",
                            "int",
                            "long",
                            "decimal"
                        ],
                        "description": "Latitude must be a number."
                    },
                    "location_long": {
                        "bsonType": [
                            "double",
                            "int",
                            "long",
                            "decimal"
                        ],
                        "description": "Longitude must be a number."
                    }
                }
            }
        }

        try:
            # Apply the rules to the existing animals collection.
            self.database.command({
                "collMod": self.collection.name,
                "validator": validation_rules,
                "validationLevel": "moderate",
                "validationAction": "error"
            })

            return True

        except PyMongoError as error:
            raise RuntimeError(
                "The database validation rules could not be applied."
            ) from error