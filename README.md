# PopularMovies_Stage1
Udacity Nanodegree - Project 1

In PopularMovies_Stage1/PopularMovies/app/build.gradle file, 
replace --api--key-- in:

            buildTypes.each {
                        it.buildConfigField 'String', 'MOVIE_DATABASE_API_KEY', "\"--api--key--\""
                    }
        
with your own API key.
