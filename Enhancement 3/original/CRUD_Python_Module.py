# Example Python Code to Insert a Document 

from pymongo import MongoClient 
from bson.objectid import ObjectId 

class AnimalShelter(object): 
    """ CRUD operations for Animal collection in MongoDB """ 

    def __init__(self, USER, PASS): 
        # Initializing the MongoClient. This helps to access the MongoDB 
        # databases and collections. This is hard-wired to use the aac 
        # database, the animals collection, and the aac user. 
        # 
        # You must edit the password below for your environment. 
        # 
        # Connection Variables 
        # 
        
        HOST = 'localhost' 
        PORT = 27017 
        DB = 'aac' 
        COL = 'animals' 
        # 
        # Initialize Connection 
        # 
        self.client = MongoClient('mongodb://localhost:27017') 
        self.database = self.client['%s' % (DB)] 
        self.collection = self.database['%s' % (COL)] 
    print('Connected')
    # Create a method to return the next available record number for use in the create method
            
    # Complete this create method to implement the C in CRUD. 
    def create(self, data):
        if data is None:
            #empty data returns false
            return False
        try:
            self.database.animals.insert_one(data)
            return True
        except:
            return False
       

    # Create method to implement the R in CRUD.
    def read(self, query):
        if query is None:
            #returns empty list
            return [] 
        try:
            cursor = self.database.animals.find(query)  # data should be dictionary
            return list(cursor)
        except:
            return []
    # create update method
    def update(self, query, newinput):
        if query is None or newinput is None:
            return 0
        try:
            result = self.database.animals.update_many(query, {"$set": newinput})
            return result.modified_count
        except:
            return 0
        #create delete method
    def delete(self, query):
        if query is None:
            return 0
        try:
            result = self.database.animals.delete_many(query)
            return result.deleted_count
        except:
            return 0