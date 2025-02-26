SRC=%1%
grep -e "INTO movies " %SRC%|sed -e "s/INSERT INTO movies VALUES(//" -e "s/);//" > movies.csv
grep -e "INTO stars " %SRC%|sed -e "s/INSERT INTO stars (id, name) VALUES(\(.*\));/\1,\\\\N/" -e "s/INSERT INTO stars (id, name, birthYear) VALUES(\(.*\));/\1/" > stars.csv
grep -e "INTO stars_in_movies " %SRC%|sed -e "s/INSERT INTO stars_in_movies VALUES(//" -e "s/);//" > stars_in_movies.csv
grep -e "INTO genres " %SRC%|sed -e "s/INSERT INTO genres VALUES(//" -e "s/);//" > genres.csv
grep -e "INTO genres_in_movies " %SRC%|sed -e "s/INSERT INTO genres_in_movies VALUES(//" -e "s/);//" > genres_in_movies.csv
grep -e "INTO creditcards " %SRC%|sed -e "s/INSERT INTO creditcards VALUES(//" -e "s/);//" > creditcards.csv
grep -e "INTO customers " %SRC%|sed -e "s/INSERT INTO customers VALUES(//" -e "s/);//" > customers.csv
grep -e "INTO sales " %SRC%|sed -e "s/INSERT INTO sales VALUES(//" -e "s/);//" > sales.csv
grep -e "INTO ratings " %SRC%|sed -e "s/INSERT INTO ratings VALUES(//" -e "s/);//" > ratings.csv
