# FacebookAPIAndroid
React-Native Facebook Login (via LoginManager) &amp; Graph API Modules - for Android


##Methods

###Login

* loginWithReadPermissions
* loginWithPublishPermissions
* logout
* getCurrentToken

###Usage

```
...
var FBLogin = require('./FacebookLogin.js');
...
var FacebookAPIAndroid = React.createClass({

  _loginPress: function() {
    console.log("FB Login Start");
    FBLogin.loginWithReadPermissions(['public_profile','email'], result=> {
      console.log(result);
    });
  },

  render: function() {
    return (
        <View>
          <TouchableOpacity onPress={() => {this._loginPress()}}>
            <Text>Login</Text>
          </TouchableOpacity>
        </View>
    );
  }
});
```

###Graph

* search

###Usage
(When you have logged in)
```
...
var FBGraph = require('./FacebookGraph.js');
...
var FacebookAPIAndroid = React.createClass({

  _buttonPress: function() {
    FBGraph.search('place','/search','golf club', 52.4796206,-1.8824216, 0, result=> {
      console.log(result);
    });
  },

  render: function() {
    return (
        <View>
          <TouchableOpacity onPress={() => {this._buttonPress()}}>
            <Text>Do Search</Text>
          </TouchableOpacity>
        </View>
    );
  }
});
```
