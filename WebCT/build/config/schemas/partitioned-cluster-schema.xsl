<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<xsl:output method="xml" encoding="UTF-8" omit-xml-declaration="no" indent="yes"/>
<xsl:template match="/">
	<cluster-config>
		<xsl:copy-of select="cluster-config/cluster-name"/>
		<xsl:copy-of select="cluster-config/dist-cache"/>
		<xsl:copy-of select="cluster-config/jms"/>
		<description>The partitioned topology provides best performance for large data sets. This topology allows to you store unlimited amount of data within the cluster by utilizing the accumulated memory size of all cluster machines. The partitioned topology routes the application's operations into difference spaces, thus storing different portions of the data in separate spaces.</description>
		<notify-recovery>true</notify-recovery>
		<cache-loader>
			<external-data-source>${com.gs.cluster.cache-loader.external-data-source}</external-data-source>
			<central-data-source>${com.gs.cluster.cache-loader.central-data-source}</central-data-source>
		</cache-loader>
		<cluster-members>
			<xsl:copy-of select="cluster-config/cluster-members/member"/>
		</cluster-members>
		<groups>
			<group>
				<group-name>partitioned_hashbased_group</group-name>
					<group-members>
						<xsl:copy-of select="cluster-config/cluster-members/member"/>
					</group-members>
					<load-bal-policy>
						<apply-ownership>false</apply-ownership>						
						<disable-parallel-scattering>false</disable-parallel-scattering>
						<proxy-broadcast-threadpool-min-size>4</proxy-broadcast-threadpool-min-size>
						<proxy-broadcast-threadpool-max-size>64</proxy-broadcast-threadpool-max-size>						
						
						<default>
							<policy-type>hash-based</policy-type>
							<broadcast-condition>routing-index-is-null</broadcast-condition>
						</default>						
					</load-bal-policy>
					<fail-over-policy>
						<fail-back>false</fail-back>
				    	<fail-over-find-timeout>2000</fail-over-find-timeout>
				    	<default>
						 	<policy-type>fail-in-group</policy-type>
						 	
				    	</default>
						<active-election>
							<connection-retries>60</connection-retries>
							<yield-time>1000</yield-time>
						    <fault-detector>
						        <invocation-delay>1000</invocation-delay>
						        <retry-count>3</retry-count>
						        <retry-timeout>100</retry-timeout>
						   </fault-detector>   		
						</active-election>							
		       		</fail-over-policy>
				</group>
			</groups>
	</cluster-config>
</xsl:template>
</xsl:stylesheet>