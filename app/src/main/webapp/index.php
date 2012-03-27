<html>
<head>
<title>
Bianca&#153; Start Page
</title>

<!--
<?php

  function bianca_test()
  {
    return function_exists("bianca_version");
  }

?>
-->

<style type="text/css">
.message {
  margin: 10px;
  padding: 10px;
  border: 1px solid blue;
  background: #CCCCCC;
}

.footer {
  font-size: small;
  font-style: italic;
}

#failure {
    <?php echo "display: none;"; ?> 
}

#failure_default_interpreter {
    display: none;
    <?php if (! bianca_test()) echo "display: block;"; ?> 
}

#success_pro {
    display: none;
    <?php if (bianca_is_pro() && bianca_test()) echo "display: block;"; ?> 
}

#success_open_source {
    display: none;
    <?php if (! bianca_is_pro() && bianca_test()) echo "display: block;"; ?> 
}
</style>
</head>

<body>

<p>
Testing for Bianca&#153;...
</p>

<div class="message" id="failure">
PHP files are not being interpreted by Bianca&#153;.
</div>

<div class="message" id="failure_default_interpreter">
PHP is being interpreted, but not by Bianca&#153;!  Please check your configuration.
</div>

<div class="message" id="success_pro">
Congratulations!  Bianca&#153; <?php if (bianca_test()) echo bianca_version(); ?> is compiling PHP pages.  Have fun!
</div>

<div class="message" id="success_open_source">
Congratulations!  Bianca&#153; <?php if (bianca_test()) echo bianca_version(); ?> is interpreting PHP pages.  Have fun!
</div>

<div>
Documentation is available at <a href="http://www.biancaproject.com">http://www.biancaproject.com</a>
</div>

<hr/>

<div class="footer">
Copyright &copy; 2011-2012
<a href="http://www.clever-cloud.com">Clever Cloud SAS</a>. 
All rights reserved.<br/>
Copyright &copy; 1998-2010
<a href="http://www.caucho.com">Caucho Technology, Inc</a>. 
All rights reserved.<br/>

Resin <sup><font size="-1">&#174;</font></sup> is a registered trademark of Caucho Technology.
</div>
</body>

</html>
