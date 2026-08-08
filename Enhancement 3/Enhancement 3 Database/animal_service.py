import pandas as pd


class AnimalService:
    # These are the animal fields that will appear in the dashboard table.
    DISPLAY_FIELDS = [
        "age_upon_outcome",
        "animal_id",
        "animal_type",
        "breed",
        "color",
        "date_of_birth",
        "datetime",
        "monthyear",
        "name",
        "outcome_subtype",
        "outcome_type",
        "sex_upon_outcome",
        "location_lat",
        "location_long",
        "age_upon_outcome_in_weeks"
    ]
    # These are the form fields used for the database enhancement.
    FORM_FIELDS = [
        "animal_id",
        "name",
        "animal_type",
        "breed",
        "color",
        "sex_upon_outcome",
        "age_upon_outcome_in_weeks",
        "location_lat",
        "location_long"
    ]

    # These fields should be treated as numbers.
    NUMBER_FIELDS = [
        "age_upon_outcome_in_weeks",
        "location_lat",
        "location_long"
    ]

    # These MongoDB queries define the requirements for each rescue type.
    RESCUE_FILTERS = {
        "All": {},

        # Water rescue dogs must match the selected breeds, sex, and age range.
        "WaterRescue": {
            "animal_type": "Dog",
            "breed": {
                "$in": [
                    "Labrador Retriever Mix",
                    "Chesapeake Bay Retriever",
                    "Newfoundland"
                ]
            },
            "sex_upon_outcome": "Intact Female",
            "age_upon_outcome_in_weeks": {
                "$gte": 26,
                "$lte": 156
            }
        },

        # Mountain and wilderness rescue dogs must match these requirements.
        "MWR": {
            "animal_type": "Dog",
            "breed": {
                "$in": [
                    "German Shepherd",
                    "Alaskan Malamute",
                    "Old English Sheepdog",
                    "Siberian Husky",
                    "Rottweiler"
                ]
            },
            "sex_upon_outcome": "Intact Male",
            "age_upon_outcome_in_weeks": {
                "$gte": 26,
                "$lte": 156
            }
        },

        # Disaster and individual tracking dogs must match these requirements.
        "DIT": {
            "animal_type": "Dog",
            "breed": {
                "$in": [
                    "Doberman Pinscher",
                    "German Shepherd",
                    "Golden Retriever",
                    "Bloodhound",
                    "Rottweiler"
                ]
            },
            "sex_upon_outcome": "Intact Male",
            "age_upon_outcome_in_weeks": {
                "$gte": 20,
                "$lte": 300
            }
        }
    }

    def __init__(self, repository):
        # The service does not connect directly to MongoDB, it sends database requests through the repository layer.
        self.repository = repository

    def get_animals(self, filter_type):
        # Make sure the selected filter is one of the allowed choices.
        if filter_type not in self.RESCUE_FILTERS:
            raise ValueError("The selected rescue filter is invalid.")

        # Get the MongoDB query that matches the selected rescue type.
        query = self.RESCUE_FILTERS[filter_type]

        # Build a projection so MongoDB only returns the fields
        projection = {
            field: 1 for field in self.DISPLAY_FIELDS
        }

        # Do not include MongoDB's ObjectId in the results.
        projection["_id"] = 0

        #retrieve the matching animal records.
        records = self.repository.find(query, projection)

        # Clean the records before sending them to the dashboard.
        return self.clean_records(records)

    def clean_records(self, records):

        # This list will store the cleaned animal records.
        cleaned_records = []

        # Check each record returned from MongoDB.
        for record in records:
            cleaned_record = {}

            # Copy each required field into the cleaned record.
            for field in self.DISPLAY_FIELDS:
                value = record.get(field)

                # Replace missing values so blank cells do not appear.
                if value is None:
                    value = "Unknown"

                cleaned_record[field] = value

            # Add the cleaned record to the final list.
            cleaned_records.append(cleaned_record)

        return cleaned_records

    def get_table_columns(self):
        # This list will hold the Dash column definitions.
        columns = []
        # Create one table column for each display field.
        for field in self.DISPLAY_FIELDS:
            columns.append({
                # Replace underscores and capitalize the words
                "name": field.replace("_", " ").title(),
                # The column ID must match the field name in the data.
                "id": field,
                # Users cannot delete columns from the table.
                "deletable": False,
                # Users are allowed to select a column.
                "selectable": True
            })

        return columns

    def get_breed_summary(self, records):
        # Return an empty DataFrame if there are no records.
        if not records:
            return pd.DataFrame(columns=["breed", "count"])
        # Convert the animal records into a DataFrame.
        dataframe = pd.DataFrame(records)
        # Make sure the breed field exists before creating the chart.
        if "breed" not in dataframe.columns:
            return pd.DataFrame(columns=["breed", "count"])
        # Count each breed, keep the ten most common breeds, and prepare the results for the bar chart.
        summary = (
            dataframe["breed"]
            .fillna("Unknown")
            .value_counts()
            .head(10)
            .reset_index()
        )
        # Rename the columns so Plotly can use them clearly.
        summary.columns = ["breed", "count"]
        return summary

    def get_selected_animal(self, records, selected_rows):
        # A map cannot be created when there are no records.
        if not records:
            return None
        # Use the selected row when the user has chosen one.
        if selected_rows:
            row_index = selected_rows[0]
        else:
            # Use the first animal when no row has been selected.
            row_index = 0
        # Prevent an invalid row number from causing an error.
        if row_index < 0 or row_index >= len(records):
            row_index = 0
        # Get the selected animal from the records list.
        animal = records[row_index]
        try:
            # Convert the latitude and longitude into numbers
            latitude = float(animal["location_lat"])
            longitude = float(animal["location_long"])
        except (KeyError, TypeError, ValueError):
            # Return None if the location is missing or invalid.
            return None
        # Return only the information needed by the map callback.
        return {
            "latitude": latitude,
            "longitude": longitude,
            "name": animal.get("name", "Unknown"),
            "breed": animal.get("breed", "Unknown")
        }
    def prepare_form_data(self, form_data):
        # Store the cleaned values from the dashboard form.
        cleaned_data = {}

        # Go through each field from the form.
        for field, value in form_data.items():
            if value is None:
                continue

            # Remove extra spaces from text values.
            if isinstance(value, str):
                value = value.strip()

            # Skip blank values.
            if value == "":
                continue

            # Convert number fields so MongoDB stores them correctly.
            if field in self.NUMBER_FIELDS:
                try:
                    value = float(value)
                except (TypeError, ValueError):
                    raise ValueError("{} must be a number.".format(
                        field.replace("_", " ").title()
                    ))

            cleaned_data[field] = value

        return cleaned_data

    def create_animal(self, form_data):
        # Clean the values from the dashboard form.
        cleaned_data = self.prepare_form_data(form_data)

        # animal_id is required for a new record.
        animal_id = cleaned_data.get("animal_id")
        if not animal_id:
            raise ValueError("Animal ID is required to add a record.")

        # Prevent duplicate animal IDs.
        existing_record = self.repository.find(
            {"animal_id": animal_id},
            {"animal_id": 1, "_id": 0}
        )
        if existing_record:
            raise ValueError("That Animal ID already exists.")

        return self.repository.create(cleaned_data)

    def update_animal(self, animal_id, form_data):
        # Make sure the animal_id was provided.
        if animal_id is None or str(animal_id).strip() == "":
            raise ValueError("Animal ID is required to update a record.")

        # Clean the values from the dashboard form.
        cleaned_data = self.prepare_form_data(form_data)

        # The animal_id is used to find the record.
        # It should not be updated as part of the new values.
        cleaned_data.pop("animal_id", None)

        # Require at least one field to update.
        if not cleaned_data:
            raise ValueError("Enter at least one value to update.")

        updated_count = self.repository.update(
            {"animal_id": str(animal_id).strip()},
            cleaned_data
        )

        if updated_count == 0:
            raise ValueError("No matching animal record was updated.")

        return updated_count

    def delete_animal(self, animal_id):
        # Make sure the animal_id was provided.
        if animal_id is None or str(animal_id).strip() == "":
            raise ValueError("Animal ID is required to delete a record.")

        deleted_count = self.repository.delete(
            {"animal_id": str(animal_id).strip()}
        )

        if deleted_count == 0:
            raise ValueError("No matching animal record was deleted.")

        return deleted_count


    def get_database_breed_summary(self, filter_type):
        # Make sure the selected filter is valid.
        if filter_type not in self.RESCUE_FILTERS:
            raise ValueError("The selected rescue filter is invalid.")

        # MongoDB will filter, group, sort, and limit the results.
        pipeline = [
            {"$match": self.RESCUE_FILTERS[filter_type]},
            {
                "$group": {
                    "_id": "$breed",
                    "count": {"$sum": 1}
                }
            },
            {"$sort": {"count": -1}},
            {"$limit": 10},
            {
                "$project": {
                    "_id": 0,
                    "breed": {
                        "$ifNull": ["$_id", "Unknown"]
                    },
                    "count": 1
                }
            }
        ]

        records = self.repository.aggregate(pipeline)

        return pd.DataFrame(
            records,
            columns=["breed", "count"]
        )
    def records_to_csv(self, records):
        # Prevent the program from creating an empty CSV file.
        if not records:
            raise ValueError("There are no records to export.")
        # Convert the visible animal records into a DataFrame.
        dataframe = pd.DataFrame(records)
        # Convert the DataFrame into CSV text without row numbers.
        return dataframe.to_csv(index=False)