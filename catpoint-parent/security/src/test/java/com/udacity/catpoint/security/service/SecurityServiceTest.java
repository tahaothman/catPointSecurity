package com.udacity.catpoint.security.service;

import com.google.common.base.Verify;
import com.udacity.catpoint.image.service.ImageService;
import com.udacity.catpoint.security.application.StatusListener;
import com.udacity.catpoint.security.data.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.verification.VerificationMode;

import java.awt.image.BufferedImage;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)

public class SecurityServiceTest {
    @Mock
    Sensor sensor;
    @Mock
    ImageService imageService;
    @Mock
    SecurityRepository securityRepository;

    @InjectMocks
    SecurityService securityService;

    @BeforeEach
    public void setupEach(){
        MockitoAnnotations.openMocks(this);
    }


    @ParameterizedTest
    @EnumSource(value= ArmingStatus.class, names = {"ARMED_AWAY","ARMED_HOME"})
    public void when_alramArmed_and_seneorActivited(ArmingStatus armingStatus) {
        when(securityRepository.getArmingStatus()).thenReturn(armingStatus);
        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.NO_ALARM);
        when(sensor.getActive()).thenReturn(false);
        boolean activeFlag = true;
        securityService.changeSensorActivationStatus(sensor, activeFlag);


        verify(securityRepository,times(1)).setAlarmStatus(AlarmStatus.PENDING_ALARM);
        verify(sensor, times(1)).setActive(any(Boolean.TYPE));
        verify(sensor, times(1)).getActive();
        verify(securityRepository, times(1)).updateSensor(any(Sensor.class));

    }
//    2-If alarm is armed and a sensor becomes activated and the system is already pending alarm, set the alarm status to alarm.
    @ParameterizedTest  //Test NO.2
    @EnumSource(value= ArmingStatus.class, names = {"ARMED_AWAY","ARMED_HOME"})
    public void when_alarmArmed_and_sensorActivated_andSysetemInPendingAlarm(ArmingStatus armingStatus){
        when(securityRepository.getArmingStatus()).thenReturn(armingStatus);
        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.PENDING_ALARM);
        when(sensor.getActive()).thenReturn(false);

        boolean activeFlag= true;
        securityService.changeSensorActivationStatus(sensor, activeFlag);
        verify(securityRepository,times(1)).setAlarmStatus(AlarmStatus.ALARM);
        verify(sensor, times(1)).setActive(any(Boolean.TYPE));
        verify(sensor, times(1)).getActive();
        verify(securityRepository, times(1)).updateSensor(any(Sensor.class));


    }
//    3-If pending alarm and all sensors are inactive, return to no alarm state.
    @Test
    public void when_alarmsStatusAsPendingAlarm_and_sensorsInactive(){
        when(sensor.getActive()).thenReturn(true);
        boolean systemFlag= false;
        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.PENDING_ALARM);

        securityService.changeSensorActivationStatus(sensor,systemFlag);

        verify(sensor, times(1)).setActive(any(Boolean.TYPE));
        verify(sensor, times(2)).getActive();
        verify(securityRepository, times(1)).updateSensor(any(Sensor.class));
        verify(securityRepository,times(1)).setAlarmStatus(AlarmStatus.NO_ALARM);

    }

//   4-If alarm is active, change in sensor state should not affect the alarm state.
    @ParameterizedTest
    @ValueSource(booleans = {true,false})
    public void when_alarmIsActive_and_changeSeneorState_shouldNotAffectState(boolean sensorState){

//        when(sensor.getActive()).thenReturn(sensorState);
        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.ALARM);
        securityService.changeSensorActivationStatus(sensor,!sensorState);

        verify(sensor, times(1)).setActive(any(Boolean.TYPE));
        verify(sensor, never()).getActive();
        verify(securityRepository, times(1)).updateSensor(any(Sensor.class));
        verify(securityRepository,never()).setAlarmStatus(AlarmStatus.NO_ALARM);
    }


//    5-If a sensor is activated while already active and the system is in pending state, change it to alarm state.

    @Test
    public void when_sensorActivated_and_SystemActive_alarmInPendingState(){

        when(sensor.getActive()).thenReturn(false);
        boolean systemFlag=true;
        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.PENDING_ALARM);

        securityService.changeSensorActivationStatus(sensor, systemFlag);

        verify(sensor, times(1)).setActive(any(Boolean.TYPE));
        verify(securityRepository, times(1)).updateSensor(any(Sensor.class));
        verify(securityRepository,times(1)).setAlarmStatus(AlarmStatus.ALARM);




    }

    //test commented
//    @Test
//    public test5(){
//        when(sensor.getActive()).thenReturn(true);
//        boolean systemFlag = true;
//        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.PENDING_ALARM);
//
//        verify(sensor, times(1)).setActive(any(Boolean.TYPE));
//        verify(securityRepository, times(1)).updateSensor(any(Sensor.class));
//        verify(securityRepository,times(1)).setAlarmStatus(AlarmStatus.ALARM);
//
//
//    }

//    6-If a sensor is deactivated while already inactive, make no changes to the alarm state.

    @Test
    public void when_sensorDEActivated_andSystemAlreadyActive(){
        when(sensor.getActive()).thenReturn(false);
        boolean systemFlag= false;
//        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.PENDING_ALARM);

        securityService.changeSensorActivationStatus(sensor, systemFlag);

        verify(sensor, times(1)).setActive(any(Boolean.TYPE));
        verify(securityRepository, times(1)).updateSensor(any(Sensor.class));
        verify(securityRepository,never()).setAlarmStatus(any(AlarmStatus.class));

    }


//    7-If the image service identifies an image containing a cat while the system is armed-home, put the system into alarm status.
    @Test
    public void when_imageService_find_a_cat(){
        when(imageService.imageContainsCat(any(BufferedImage.class), anyFloat())).thenReturn(true);
        when(securityRepository.getArmingStatus()).thenReturn(ArmingStatus.ARMED_HOME);

        securityService.processImage(mock(BufferedImage.class));

        verify(securityRepository, times(1)).setAlarmStatus(AlarmStatus.ALARM);

    }

//    8-If the image service identifies an image that does not contain a cat, change the status to no alarm as long as the sensors are not active.

    @Test
    public void when_imageService_findNo_cat(){
        when(imageService.imageContainsCat(any(BufferedImage.class),anyFloat())).thenReturn(false);
//        Set<Sensor> inactiveSensors = createSensors(false, 5 );
//        when(securityRepository.getSensors()).thenReturn(inactiveSensors);
        securityService.processImage(mock(BufferedImage.class));

        verify(securityRepository, times(1)).setAlarmStatus(AlarmStatus.NO_ALARM);




    }

    private Set<Sensor> createSensors(boolean sensorActiveState, int numberOfSensors) {
        Set<Sensor> sensors = new HashSet<>();
        for(int i =0; i< numberOfSensors; i++){
            Sensor newSensor = new Sensor(getRandomString(), SensorType.randomSensorType());
            newSensor.setActive(sensorActiveState);
            sensors.add(newSensor);
        }
        return sensors;

    }

    private String getRandomString() {
        return UUID.randomUUID().toString();
    }


//    9-If the system is disarmed, set the status to no alarm.
    @Test
    public void when_systemIsDisArmed(){
        securityService.setArmingStatus(ArmingStatus.DISARMED);
        verify(securityRepository, times(1)).setAlarmStatus(AlarmStatus.NO_ALARM);
        verify(securityRepository, times(1)).setArmingStatus(ArmingStatus.DISARMED);
    }





//    10-If the system is armed, reset all sensors to inactive.

    @ParameterizedTest
    @EnumSource(value= ArmingStatus.class, names = {"ARMED_AWAY","ARMED_HOME"})
    public void when_systemIsArmed_resetAllSensorstoInactive(ArmingStatus armingStatus){
        Set<Sensor> activeSensors = createSensors(true, 8 );
        when(securityRepository.getSensors()).thenReturn(activeSensors);

        securityService.setArmingStatus(armingStatus);
        verify(securityRepository,  times(1)).setArmingStatus(armingStatus);
        verify(securityRepository, times(1)).getSensors();
        assertAllSensorsMatchInputActive(activeSensors, false);



    }

    private void assertAllSensorsMatchInputActive(Set<Sensor> sensors, boolean sensorActiveStatus) {
        sensors.forEach(sensor1 -> assertEquals(sensorActiveStatus,sensor1.getActive()));
    }


//    11-If the system is armed-home while the camera shows a cat, set the alarm status to alarm.

    @Test
    public void when_systemIsArmedHome_and_cameraShowsCat(){
        when(securityRepository.getArmingStatus()).thenReturn(ArmingStatus.ARMED_HOME);
        when(imageService.imageContainsCat(any(BufferedImage.class),anyFloat())).thenReturn(true);

        securityService.processImage(mock(BufferedImage.class));
        verify(securityRepository, times(1)).setAlarmStatus(AlarmStatus.ALARM);


    }



    @Test
    public void test_add_delete_statusListeners(){
        StatusListener mockStatusListener = mock(StatusListener.class);
        securityService.addStatusListener(mockStatusListener);
        securityService.removeStatusListener(mockStatusListener);
    }
    @Test
    public void test_add_sensor_method(){
        Sensor mockSensor = mock(Sensor.class);
        securityService.addSensor(mockSensor);
        securityService.removeSensor(mockSensor);
        verify(securityRepository, times(1)).addSensor(any(Sensor.class));
        verify(securityRepository, times(1)).removeSensor(any(Sensor.class));
    }
    @Test
    public void test_getAlarmStaus_(){
        securityService.getAlarmStatus();
        verify(securityRepository, times(1)).getAlarmStatus();
    }
    @Test
    public void when_sensorisActive(){
        when(sensor.getActive()).thenReturn(true);
        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.ALARM);

        securityService.changeSensorActivationStatus(sensor);

        verify(sensor, times(1)).getActive();
        verify(securityRepository, times(1)).updateSensor(any(Sensor.class));
        verify(securityRepository, times(1)).setAlarmStatus(AlarmStatus.PENDING_ALARM);
    }
    @Test
    public void when_sensorisNotActive(){
        when(sensor.getActive()).thenReturn(false);
        when(securityRepository.getArmingStatus()).thenReturn(ArmingStatus.DISARMED);

        securityService.changeSensorActivationStatus(sensor);

        verify(sensor, times(1)).getActive();
        verify(securityRepository, times(1)).updateSensor(any(Sensor.class));
        verify(securityRepository, never()).setAlarmStatus(AlarmStatus.PENDING_ALARM);
    }
    @Test
    public void test_securityServices(){
        SecurityService securityServices = new SecurityService(securityRepository);
    }

    @Test
    public void armedHome_catFound_systemAlarmed() {
        securityService.catFoundFlag = true;
        securityService.setArmingStatus(ArmingStatus.ARMED_HOME);
    }

    @ParameterizedTest
    @EnumSource(value = ArmingStatus.class, names = {"ARMED_HOME", "ARMED_AWAY"})
    public void systemArmed_sensorInactive(ArmingStatus armingStatus) {
        when(securityRepository.getArmingStatus()).thenReturn(armingStatus);

        securityService.changeSensorActivationStatus(sensor);

        verify(sensor, times(1)).setActive(false);
    }

    @Test
    public void sensorInactive_alarmPending_setNoAlarm() {
        when(sensor.getActive()).thenReturn(false);
        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.PENDING_ALARM);

        securityService.changeSensorActivationStatus(sensor);

        verify(securityRepository, times(1)).setAlarmStatus(AlarmStatus.NO_ALARM);
    }







}
