import os
# dotenv loads the values stored inside the .env file
from dotenv import load_dotenv
from pymongo import MongoClient
from pymongo.errors import PyMongoError
# Load the environment variables before connecting to MongoDB
load_dotenv()
class AnimalRepository:
    def __init__(self):
 # Read the MongoDB connection address from the .env file.
# The local address is used as a backup value.
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

    def find(self, query=None, projection=None):
        #returns records

        try:              # This returns all animal records.
            cursor = self.collection.find(query or {}, projection)

            # Convert the MongoDB cursor into a normal Python list.
            return list(cursor)

        except PyMongoError as error:
            # error for not finding records
            raise RuntimeError("Animal records could not be retrieved.") from error

    def create(self, data):
        #Insert one animal record.

        # Make sure the new animal data is not empty
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
            # Update every animal record that matches the query.
            # The $set command changes only the chosen fields.
            result = self.collection.update_many(query, {"$set": new_values})

            # Return the number of records that were changed.
            return result.modified_count

        except PyMongoError as error:
            #error if the update fails.
            raise RuntimeError("The animal records could not be updated.") from error

    def delete(self, query):
    #Delete records
    # Require a query so the program does not accidentally
    # delete every record in the collection.
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