"""Seed the pilot catalog with sample garments.

This creates catalog ROWS only. Drop matching product photos into
media/catalog/, named exactly as listed below (e.g. grm_001.jpg), before
testing the try-on flow — these are placeholders to get the app running
end to end. Replace SAMPLE_GARMENTS with your real product data (or point
the catalog router at your actual inventory system) before the pilot goes
live in-store.

Usage: python seed_catalog.py
"""
from app.database import Base, SessionLocal, engine
from app import models

SAMPLE_GARMENTS = [
    {"id": "grm_001", "name": "Floral Wrap Dress", "category": "dresses", "price": 2499.0, "image": "grm_001.jpg"},
    {"id": "grm_002", "name": "Classic White Shirt", "category": "tops", "price": 1299.0, "image": "grm_002.jpg"},
    {"id": "grm_003", "name": "Slim Fit Denim", "category": "bottoms", "price": 1899.0, "image": "grm_003.jpg"},
    {"id": "grm_004", "name": "Tailored Blazer", "category": "outerwear", "price": 3499.0, "image": "grm_004.jpg"},
]


def main():
    Base.metadata.create_all(bind=engine)
    db = SessionLocal()
    try:
        added = 0
        for g in SAMPLE_GARMENTS:
            if db.get(models.Garment, g["id"]) is not None:
                continue
            db.add(models.Garment(
                id=g["id"], name=g["name"], category=g["category"],
                price=g["price"], currency="INR", image_filename=g["image"],
            ))
            added += 1
        db.commit()
        print(f"Seeded {added} new garment(s) (of {len(SAMPLE_GARMENTS)} total in this script).")
        print("Remember to add media/catalog/<image> for each one.")
    finally:
        db.close()


if __name__ == "__main__":
    main()
