import pandas as pd
import pickle
from sklearn.cluster import KMeans
from sklearn import preprocessing


def predict_suggestions(user_data, km, scaler, cluster_suggestions):
    assert(list(user_data.keys()) == ['INCOME', 'REGION', 'SEX', 'AGE', 
        'MARRIAGE', 'SAVINGS', 'FOOD', 'SHOPPING', 'HOUSING', 
        'TRANSPORT', 'OTHERS'])

    region_map = {'Sudeste': 1, 'Centro-Oeste': 2, 'Sul': 3, 'Norte': 4, 'Nordeste': 5}
    sex_map = {'Male': 1, 'Female': 2}
    marriage_map = {'Married': 1, 'Widowed': 2, 'Single': 3, 'Divorced/Separated': 4}

    user_data['SEX'] = sex_map[user_data['SEX']]
    user_data['REGION'] = region_map[user_data['REGION']]
    user_data['MARRIAGE'] = marriage_map[user_data['MARRIAGE']]

    for k in user_data:
        user_data[k] = [user_data[k]]
    user_data = pd.DataFrame.from_dict(user_data)
    user_data = scaler.transform(user_data)

    label = km.predict(user_data)[0]
    return cluster_suggestions[label]


if __name__ == '__main__':
    user_data = {'INCOME': 11318, 'REGION': 'Sudeste', 
                 'SEX': 'Male', 'AGE': 49, 'MARRIAGE': 'Single', 
                 'SAVINGS': 4319, 'FOOD': 2776, 'SHOPPING': 911, 
                 'HOUSING': 1499, 'TRANSPORT': 112, 'OTHERS': 1171}
    f = open('kmeans_data.pickle', 'rb')
    km, scaler, cluster_suggestions = pickle.load(f)
    print(predict_suggestions(user_data, km, scaler, cluster_suggestions))
 
