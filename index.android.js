'use strict';

var React = require('react-native');

var {
  AppRegistry,
  StyleSheet,
  Text,
  View,
} = React;

var FBLogin = require('./FacebookLogin.js');
var FBGraph = require('./FacebookGraph.js');

var FacebookAPIAndroid = React.createClass({

  _loginPress: function() {
    console.log("FB Login Start");
    FBLogin.loginWithReadPermissions(['public_profile','email'], result=> {
      console.log(result);
    });
  },

  _searchPress: function() {
    console.log("FB Search Start");
    FBGraph.search('place','/search','golf club', 52.4855570,-1.9064510, 0, result=> {
      console.log(result);
    });
  },
  
  render: function() {
    return (
        <View style={styles.container}>
          <Text style={styles.welcome}>Facebook Test</Text>
          <Text style={styles.instructions}>Click to Login. Output will be logged to console in debugger. Once logged in, click `Run Test Search` to carry out a GraphAPI places lookup.</Text>
          <TouchableOpacity onPress={() => {this._loginPress()}} style={{backgroundColor:'#aaa',padding:20}}>
            <Text style={{color:'white'}}>Login</Text>
          </TouchableOpacity>
          <TouchableOpacity onPress={() => {this._searchPress()}} style={{backgroundColor:'#aaa',padding:20,marginTop:50}}>
            <Text style={{color:'white'}}>Run Test Search</Text>
          </TouchableOpacity>
        </View>
    );
  }
});

AppRegistry.registerComponent('FacebookAPIAndroid', () => FacebookAPIAndroid);


const styles = StyleSheet.create({
  container: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    backgroundColor: '#F5FCFF',
  },
  welcome: {
    fontSize: 20,
    textAlign: 'center',
    margin: 10,
  },
  instructions: {
    textAlign: 'center',
    color: '#333333',
    marginBottom: 5,
  },
});

AppRegistry.registerComponent('FacebookAPIAndroid', () => FacebookAPIAndroid);
