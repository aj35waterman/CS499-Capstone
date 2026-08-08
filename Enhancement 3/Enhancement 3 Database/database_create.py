# This file creates the database indexes and validation rules
from animal_repository import AnimalRepository


# Connect to MongoDB through the repository layer
repository = AnimalRepository()


try:
    # Create indexes for the fields used by the dashboard filters
    index_names = repository.create_indexes()

    print("Database indexes were checked.")

    if index_names:
        for index_name in index_names:
            print(index_name)
    else:
        print("All database indexes already exist.")

    # Apply the MongoDB schema validation rules
    repository.apply_schema_validation()

    print("Database validation rules were applied.")

except RuntimeError as error:
    print(error)