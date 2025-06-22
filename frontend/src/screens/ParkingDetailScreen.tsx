import React, { useState, useEffect } from 'react';
import {
  View,
  StyleSheet,
  ScrollView,
  Alert,
  Dimensions,
} from 'react-native';
import {
  Card,
  Title,
  Paragraph,
  Button,
  Chip,
  ActivityIndicator,
  Text,
  Divider,
} from 'react-native-paper';
import { useRoute, useNavigation } from '@react-navigation/native';
import { RouteProp } from '@react-navigation/native';
import { StackNavigationProp } from '@react-navigation/stack';
import MapView, { Marker, PROVIDER_GOOGLE } from 'react-native-maps';

import { parkingApi } from '../services/api';
import { ParkingLot } from '../types';
import { RootStackParamList } from '../../App';

type ParkingDetailRouteProp = RouteProp<RootStackParamList, 'ParkingDetail'>;
type ParkingDetailNavigationProp = StackNavigationProp<RootStackParamList, 'ParkingDetail'>;

const { width } = Dimensions.get('window');

const ParkingDetailScreen = () => {
  const route = useRoute<ParkingDetailRouteProp>();
  const navigation = useNavigation<ParkingDetailNavigationProp>();
  const { parkingId } = route.params;

  const [loading, setLoading] = useState(true);
  const [parkingLot, setParkingLot] = useState<ParkingLot | null>(null);
  const [availability, setAvailability] = useState<any>(null);

  useEffect(() => {
    loadParkingDetail();
  }, [parkingId]);

  const loadParkingDetail = async () => {
    try {
      setLoading(true);
      const [lotData, availabilityData] = await Promise.all([
        parkingApi.getById(parkingId),
        parkingApi.getAvailability(parkingId),
      ]);
      setParkingLot(lotData);
      setAvailability(availabilityData);
    } catch (error) {
      console.error('駐車場詳細の取得に失敗しました:', error);
      Alert.alert('エラー', '駐車場詳細の取得に失敗しました');
    } finally {
      setLoading(false);
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

  const handleReservation = () => {
    navigation.navigate('Reservation', { parkingId });
  };

  if (loading) {
    return (
      <View style={styles.loadingContainer}>
        <ActivityIndicator size="large" color="#1976d2" />
        <Text style={styles.loadingText}>読み込み中...</Text>
      </View>
    );
  }

  if (!parkingLot) {
    return (
      <View style={styles.errorContainer}>
        <Text>駐車場情報が見つかりません</Text>
      </View>
    );
  }

  return (
    <ScrollView style={styles.container}>
      {/* 地図表示 */}
      <View style={styles.mapContainer}>
        <MapView
          provider={PROVIDER_GOOGLE}
          style={styles.map}
          initialRegion={{
            latitude: parkingLot.latitude,
            longitude: parkingLot.longitude,
            latitudeDelta: 0.005,
            longitudeDelta: 0.005,
          }}
        >
          <Marker
            coordinate={{
              latitude: parkingLot.latitude,
              longitude: parkingLot.longitude,
            }}
            title={parkingLot.name}
            description={parkingLot.address}
            pinColor={getAvailabilityColor(parkingLot.availableSpaces, parkingLot.totalSpaces)}
          />
        </MapView>
      </View>

      {/* 駐車場情報 */}
      <Card style={styles.card}>
        <Card.Content>
          <Title style={styles.title}>{parkingLot.name}</Title>
          <Paragraph style={styles.address}>{parkingLot.address}</Paragraph>
          
          <View style={styles.availabilityContainer}>
            <Chip
              mode="outlined"
              style={[
                styles.availabilityChip,
                { borderColor: getAvailabilityColor(parkingLot.availableSpaces, parkingLot.totalSpaces) }
              ]}
              textStyle={{
                color: getAvailabilityColor(parkingLot.availableSpaces, parkingLot.totalSpaces),
                fontWeight: 'bold'
              }}
            >
              {getAvailabilityText(parkingLot.availableSpaces, parkingLot.totalSpaces)}
            </Chip>
          </View>

          <Divider style={styles.divider} />

          {/* 詳細情報 */}
          <View style={styles.infoContainer}>
            <View style={styles.infoRow}>
              <Text style={styles.infoLabel}>空きスペース:</Text>
              <Text style={styles.infoValue}>
                {parkingLot.availableSpaces} / {parkingLot.totalSpaces}
              </Text>
            </View>
            
            <View style={styles.infoRow}>
              <Text style={styles.infoLabel}>料金:</Text>
              <Text style={styles.infoValue}>¥{parkingLot.hourlyRate}/時間</Text>
            </View>

            <View style={styles.infoRow}>
              <Text style={styles.infoLabel}>営業状況:</Text>
              <Text style={styles.infoValue}>
                {parkingLot.isOpen ? '営業中' : '営業終了'}
              </Text>
            </View>

            {parkingLot.distance && (
              <View style={styles.infoRow}>
                <Text style={styles.infoLabel}>距離:</Text>
                <Text style={styles.infoValue}>
                  {(parkingLot.distance / 1000).toFixed(1)}km
                </Text>
              </View>
            )}
          </View>

          <Divider style={styles.divider} />

          {/* 満空情報の詳細 */}
          {availability && (
            <View style={styles.availabilityDetail}>
              <Title style={styles.sectionTitle}>満空情報</Title>
              <View style={styles.availabilityGrid}>
                {availability.spaces?.map((space: any, index: number) => (
                  <View
                    key={index}
                    style={[
                      styles.spaceIndicator,
                      { backgroundColor: space.occupied ? '#f44336' : '#4caf50' }
                    ]}
                  >
                    <Text style={styles.spaceNumber}>{index + 1}</Text>
                  </View>
                ))}
              </View>
              <View style={styles.legend}>
                <View style={styles.legendItem}>
                  <View style={[styles.legendColor, { backgroundColor: '#4caf50' }]} />
                  <Text>空き</Text>
                </View>
                <View style={styles.legendItem}>
                  <View style={[styles.legendColor, { backgroundColor: '#f44336' }]} />
                  <Text>使用中</Text>
                </View>
              </View>
            </View>
          )}

          {/* アクションボタン */}
          <View style={styles.actionContainer}>
            <Button
              mode="contained"
              onPress={handleReservation}
              style={styles.reservationButton}
              disabled={parkingLot.availableSpaces === 0}
              icon="calendar-plus"
            >
              {parkingLot.availableSpaces === 0 ? '満車' : '予約する'}
            </Button>
          </View>
        </Card.Content>
      </Card>
    </ScrollView>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#f5f5f5',
  },
  loadingContainer: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
  },
  loadingText: {
    marginTop: 16,
    fontSize: 16,
  },
  errorContainer: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
  },
  mapContainer: {
    height: 200,
  },
  map: {
    flex: 1,
  },
  card: {
    margin: 16,
    elevation: 2,
  },
  title: {
    fontSize: 24,
    fontWeight: 'bold',
    marginBottom: 8,
  },
  address: {
    fontSize: 16,
    color: '#666',
    marginBottom: 16,
  },
  availabilityContainer: {
    alignItems: 'center',
    marginBottom: 16,
  },
  availabilityChip: {
    paddingHorizontal: 16,
    paddingVertical: 8,
  },
  divider: {
    marginVertical: 16,
  },
  infoContainer: {
    marginBottom: 16,
  },
  infoRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    paddingVertical: 8,
  },
  infoLabel: {
    fontSize: 16,
    fontWeight: '500',
    color: '#333',
  },
  infoValue: {
    fontSize: 16,
    color: '#666',
  },
  availabilityDetail: {
    marginBottom: 16,
  },
  sectionTitle: {
    fontSize: 18,
    fontWeight: 'bold',
    marginBottom: 12,
  },
  availabilityGrid: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    justifyContent: 'center',
    marginBottom: 16,
  },
  spaceIndicator: {
    width: 30,
    height: 30,
    borderRadius: 15,
    justifyContent: 'center',
    alignItems: 'center',
    margin: 2,
  },
  spaceNumber: {
    color: 'white',
    fontSize: 12,
    fontWeight: 'bold',
  },
  legend: {
    flexDirection: 'row',
    justifyContent: 'center',
  },
  legendItem: {
    flexDirection: 'row',
    alignItems: 'center',
    marginHorizontal: 16,
  },
  legendColor: {
    width: 16,
    height: 16,
    borderRadius: 8,
    marginRight: 8,
  },
  actionContainer: {
    marginTop: 16,
  },
  reservationButton: {
    paddingVertical: 8,
  },
});

export default ParkingDetailScreen; 