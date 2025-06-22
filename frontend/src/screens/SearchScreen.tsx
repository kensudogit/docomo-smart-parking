import React, { useState, useEffect } from 'react';
import {
  View,
  StyleSheet,
  Alert,
  Dimensions,
} from 'react-native';
import {
  Searchbar,
  Card,
  Title,
  Paragraph,
  Button,
  Chip,
  ActivityIndicator,
  Text,
} from 'react-native-paper';
import { useNavigation } from '@react-navigation/native';
import { StackNavigationProp } from '@react-navigation/stack';
import MapView, { Marker, PROVIDER_GOOGLE } from 'react-native-maps';
import Geolocation from 'react-native-geolocation-service';

import { parkingApi } from '../services/api';
import { ParkingLot } from '../types';
import { RootStackParamList } from '../../App';

type SearchScreenNavigationProp = StackNavigationProp<RootStackParamList, 'MainTabs'>;

const { width, height } = Dimensions.get('window');

const SearchScreen = () => {
  const navigation = useNavigation<SearchScreenNavigationProp>();
  const [searchQuery, setSearchQuery] = useState('');
  const [loading, setLoading] = useState(false);
  const [parkingLots, setParkingLots] = useState<ParkingLot[]>([]);
  const [userLocation, setUserLocation] = useState<{ latitude: number; longitude: number } | null>(null);
  const [region, setRegion] = useState({
    latitude: 35.6762,
    longitude: 139.6503,
    latitudeDelta: 0.01,
    longitudeDelta: 0.01,
  });

  useEffect(() => {
    getCurrentLocation();
  }, []);

  const getCurrentLocation = () => {
    Geolocation.getCurrentPosition(
      (position) => {
        const { latitude, longitude } = position.coords;
        setUserLocation({ latitude, longitude });
        setRegion({
          latitude,
          longitude,
          latitudeDelta: 0.01,
          longitudeDelta: 0.01,
        });
        searchNearbyParkingLots(latitude, longitude);
      },
      (error) => {
        console.error('位置情報の取得に失敗しました:', error);
        Alert.alert('エラー', '位置情報の取得に失敗しました');
      },
      { enableHighAccuracy: true, timeout: 15000, maximumAge: 10000 }
    );
  };

  const searchNearbyParkingLots = async (latitude: number, longitude: number) => {
    setLoading(true);
    try {
      const lots = await parkingApi.searchNearby(latitude, longitude, 10000);
      setParkingLots(lots);
    } catch (error) {
      console.error('駐車場検索に失敗しました:', error);
      Alert.alert('エラー', '駐車場検索に失敗しました');
    } finally {
      setLoading(false);
    }
  };

  const onSearchSubmit = () => {
    if (userLocation) {
      searchNearbyParkingLots(userLocation.latitude, userLocation.longitude);
    }
  };

  const getAvailabilityColor = (available: number, total: number) => {
    const ratio = available / total;
    if (ratio > 0.5) return '#4caf50';
    if (ratio > 0.2) return '#ff9800';
    return '#f44336';
  };

  const getAvailabilityText = (available: number, total: number) => {
    const ratio = available / total;
    if (ratio > 0.5) return '空きあり';
    if (ratio > 0.2) return '少ない';
    return '満車';
  };

  return (
    <View style={styles.container}>
      {/* 検索バー */}
      <View style={styles.searchContainer}>
        <Searchbar
          placeholder="駐車場を検索"
          onChangeText={setSearchQuery}
          value={searchQuery}
          onSubmitEditing={onSearchSubmit}
          style={styles.searchBar}
        />
        <Button
          mode="contained"
          onPress={getCurrentLocation}
          style={styles.locationButton}
        >
          現在地
        </Button>
      </View>

      {/* 地図表示 */}
      <View style={styles.mapContainer}>
        <MapView
          provider={PROVIDER_GOOGLE}
          style={styles.map}
          region={region}
          onRegionChangeComplete={setRegion}
        >
          {/* ユーザーの現在位置 */}
          {userLocation && (
            <Marker
              coordinate={userLocation}
              title="現在位置"
              pinColor="blue"
            />
          )}

          {/* 駐車場マーカー */}
          {parkingLots.map((lot) => (
            <Marker
              key={lot.id}
              coordinate={{
                latitude: lot.latitude,
                longitude: lot.longitude,
              }}
              title={lot.name}
              description={`空き: ${lot.availableSpaces}/${lot.totalSpaces}`}
              pinColor={getAvailabilityColor(lot.availableSpaces, lot.totalSpaces)}
              onPress={() => {
                navigation.navigate('ParkingDetail', { parkingId: lot.id });
              }}
            />
          ))}
        </MapView>
      </View>

      {/* 駐車場リスト */}
      <View style={styles.listContainer}>
        {loading ? (
          <View style={styles.loadingContainer}>
            <ActivityIndicator size="large" color="#1976d2" />
            <Text>検索中...</Text>
          </View>
        ) : (
          <>
            <Title style={styles.listTitle}>検索結果 ({parkingLots.length}件)</Title>
            {parkingLots.map((lot) => (
              <Card key={lot.id} style={styles.card}>
                <Card.Content>
                  <Title>{lot.name}</Title>
                  <Paragraph>{lot.address}</Paragraph>
                  <View style={styles.parkingInfo}>
                    <Chip
                      mode="outlined"
                      style={[
                        styles.chip,
                        { borderColor: getAvailabilityColor(lot.availableSpaces, lot.totalSpaces) }
                      ]}
                      textStyle={{
                        color: getAvailabilityColor(lot.availableSpaces, lot.totalSpaces)
                      }}
                    >
                      {getAvailabilityText(lot.availableSpaces, lot.totalSpaces)}
                    </Chip>
                    <Chip mode="outlined" style={styles.chip}>
                      空き: {lot.availableSpaces}/{lot.totalSpaces}
                    </Chip>
                    <Chip mode="outlined" style={styles.chip}>
                      ¥{lot.hourlyRate}/時間
                    </Chip>
                    {lot.distance && (
                      <Chip mode="outlined" style={styles.chip}>
                        {(lot.distance / 1000).toFixed(1)}km
                      </Chip>
                    )}
                  </View>
                  <View style={styles.buttonContainer}>
                    <Button
                      mode="outlined"
                      onPress={() => navigation.navigate('ParkingDetail', { parkingId: lot.id })}
                      style={styles.button}
                    >
                      詳細
                    </Button>
                    <Button
                      mode="contained"
                      onPress={() => navigation.navigate('Reservation', { parkingId: lot.id })}
                      style={styles.button}
                      disabled={lot.availableSpaces === 0}
                    >
                      予約
                    </Button>
                  </View>
                </Card.Content>
              </Card>
            ))}
          </>
        )}
      </View>
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#f5f5f5',
  },
  searchContainer: {
    flexDirection: 'row',
    padding: 16,
    backgroundColor: '#fff',
    elevation: 2,
  },
  searchBar: {
    flex: 1,
    marginRight: 8,
  },
  locationButton: {
    alignSelf: 'center',
  },
  mapContainer: {
    height: height * 0.4,
  },
  map: {
    flex: 1,
  },
  listContainer: {
    flex: 1,
    padding: 16,
  },
  loadingContainer: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
  },
  listTitle: {
    marginBottom: 12,
    fontSize: 18,
    fontWeight: 'bold',
  },
  card: {
    marginBottom: 12,
    elevation: 2,
  },
  parkingInfo: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    marginTop: 8,
  },
  chip: {
    marginRight: 8,
    marginBottom: 4,
  },
  buttonContainer: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    marginTop: 12,
  },
  button: {
    flex: 1,
    marginHorizontal: 4,
  },
});

export default SearchScreen; 